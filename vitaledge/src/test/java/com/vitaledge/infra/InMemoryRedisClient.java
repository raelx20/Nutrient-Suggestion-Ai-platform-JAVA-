package com.vitaledge.infra;

import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-memory {@link RedisClient} for tests. Thread-safe; implements the same
 * sliding-window semantics as the production Lua script.
 */
public class InMemoryRedisClient implements RedisClient {

    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final Map<String, NavigableSet<Long>> windows = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public String get(String key) {
        return store.get(key);
    }

    @Override
    public void setex(String key, long ttlSeconds, String value) {
        long expiresAt = System.currentTimeMillis() + ttlSeconds * 1000L;
        store.put(key, value + "\n" + expiresAt);
    }

    @Override
    public void delete(String key) {
        store.remove(key);
        windows.remove(key);
    }

    @Override
    public long deletePattern(String pattern) {
        String regex = "^" + java.util.regex.Pattern.quote(pattern).replace("\\*", ".*") + "$";
        long deleted = 0;
        for (String key : store.keySet()) {
            if (key.matches(regex)) {
                store.remove(key);
                windows.remove(key);
                deleted++;
            }
        }
        for (String key : windows.keySet()) {
            if (key.matches(regex)) {
                windows.remove(key);
            }
        }
        return deleted;
    }

    @Override
    public boolean exists(String key) {
        String value = store.get(key);
        if (value == null) {
            return false;
        }
        return isNotExpired(key);
    }

    private boolean isNotExpired(String key) {
        String value = store.get(key);
        if (value == null) {
            return false;
        }
        int idx = value.lastIndexOf('\n');
        if (idx < 0) {
            return true;
        }
        try {
            long expiresAt = Long.parseLong(value.substring(idx + 1));
            return expiresAt > System.currentTimeMillis();
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public boolean ping() {
        return true;
    }

    @Override
    public boolean slidingWindowAllow(String key, int limit, int windowSeconds) {
        long now = System.nanoTime();
        long windowNanos = windowSeconds * 1_000_000_000L;
        lock.lock();
        try {
            NavigableSet<Long> events = windows.computeIfAbsent(key, k -> new TreeSet<>());
            events.removeIf(ts -> ts <= now - windowNanos);
            if (events.size() >= limit) {
                return false;
            }
            events.add(now);
            return true;
        } finally {
            lock.unlock();
        }
    }
}