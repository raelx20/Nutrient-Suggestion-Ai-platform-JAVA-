"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useMemo,
} from "react";
import type { UserResponse, LoginRequest, RegisterRequest } from "@/types";
import {
  authApi,
  clearTokens,
  hydrateTokens,
  ApiError,
} from "@/lib/api";

/* ================================================================
 * Auth Context — centralized authentication state.
 *
 * Stores user info and tokens in memory (not localStorage).
 * Provides login, signup, logout, and session restoration.
 * ================================================================ */

export interface AuthContextValue {
  user: UserResponse | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  error: string | null;

  login: (data: LoginRequest) => Promise<void>;
  signup: (data: RegisterRequest) => Promise<{ verificationToken: string }>;
  logout: () => Promise<void>;
  clearError: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

/* ---- Provider ---- */

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /* Attempt to restore session from the stored token pair. */
  useEffect(() => {
    let cancelled = false;

    hydrateTokens();

    async function restoreSession() {
      try {
        const me = await authApi.me();
        if (!cancelled) setUser(me);
      } catch {
        /* No valid session — stay unauthenticated. */
        if (!cancelled) clearTokens();
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    }

    restoreSession();
    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(async (data: LoginRequest) => {
    setError(null);
    setIsLoading(true);
    try {
      const res = await authApi.login(data);
      setUser(res.user);
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : "Unable to connect. Please try again.";
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const signup = useCallback(async (data: RegisterRequest) => {
    setError(null);
    setIsLoading(true);
    try {
      const res = await authApi.register(data);
      return { verificationToken: res.verification_token };
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : "Unable to connect. Please try again.";
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      /* Best-effort logout — always clear local state. */
    }
    setUser(null);
    clearTokens();
  }, []);

  const clearError = useCallback(() => setError(null), []);

  const value = useMemo(
    (): AuthContextValue => ({
      user,
      isAuthenticated: !!user,
      isAdmin: user?.roles.includes("admin") ?? false,
      isLoading,
      error,
      login,
      signup,
      logout,
      clearError,
    }),
    [user, isLoading, error, login, signup, logout, clearError]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/* ---- Hook ---- */

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
