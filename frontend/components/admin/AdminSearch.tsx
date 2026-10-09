"use client";

import { useState, useEffect, useRef, type ChangeEvent } from "react";
import { adminApi, ApiError } from "@/lib/api";
import styles from "./AdminSearch.module.css";

export interface AdminSearchProps {
  placeholder?: string;
}

/**
 * Debounced global search.
 *
 * The backend endpoint (GET /admin/search) does not exist yet.
 * The component queries it when available and otherwise shows an
 * honest "awaiting backend" empty state — no fake results.
 */
export function AdminSearch({ placeholder = "Search…" }: AdminSearchProps) {
  const [value, setValue] = useState("");
  const [open, setOpen] = useState(false);
  const [state, setState] = useState<"idle" | "loading" | "missing" | "done">(
    "idle"
  );
  const [results, setResults] = useState<{ users: unknown[]; products: unknown[] } | null>(null);
  const wrapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const onPointerDown = (e: MouseEvent) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", onPointerDown);
    return () => document.removeEventListener("mousedown", onPointerDown);
  }, [open]);

  useEffect(() => {
    if (value.trim().length < 2) return;

    let cancelled = false;
    const timer = setTimeout(async () => {
      setState("loading");
      try {
        const res = await adminApi.globalSearch(value.trim());
        if (cancelled) return;
        setResults(res);
        setState("done");
      } catch (err) {
        if (cancelled) return;
        if (err instanceof ApiError && err.status === 404) {
          setState("missing");
        } else {
          setState("missing");
        }
      }
    }, 500);

    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [value]);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const next = e.target.value;
    setValue(next);
    setOpen(true);
    if (next.trim().length < 2) {
      setState("idle");
      setResults(null);
    }
  };

  const total =
    results && state === "done"
      ? results.users.length + results.products.length
      : 0;

  return (
    <div className={styles.wrapper} ref={wrapRef}>
      <div className={styles.box}>
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
          type="search"
          className={styles.input}
          value={value}
          onChange={handleChange}
          onFocus={() => setOpen(true)}
          placeholder={placeholder}
          aria-label="Global search"
        />
      </div>

      {open && value.trim().length >= 2 && (
        <div className={styles.panel}>
          {state === "loading" && (
            <p className={styles.status}>Searching…</p>
          )}
          {state === "missing" && (
            <p className={styles.status}>
              Global search is awaiting a backend endpoint. Once{" "}
              <code>GET /admin/search</code> is available, results will appear
              here.
            </p>
          )}
          {state === "done" && results && (
            <>
              <p className={styles.status}>
                {total} result{total === 1 ? "" : "s"} for “{value}”
              </p>
              {results.users.length === 0 && results.products.length === 0 && (
                <p className={styles.status}>No matches found.</p>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}