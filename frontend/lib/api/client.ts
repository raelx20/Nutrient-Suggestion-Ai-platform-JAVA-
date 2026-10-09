/* ================================================================
 * Base API client — centralized fetch wrapper with auth handling,
 * error parsing, and token refresh.
 *
 * All API calls go through this module. Components never call
 * fetch() directly.
 * ================================================================ */

import type { ErrorResponse } from "@/types";

/** Thrown when the API returns an error response. */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly details: ErrorResponse["error"]["details"];

  constructor(status: number, body: ErrorResponse) {
    super(body.error.message);
    this.name = "ApiError";
    this.status = status;
    this.code = body.error.code;
    this.details = body.error.details;
  }
}

/* -------------------------------------------------------------- */
/* Token management.
 * Persisted in sessionStorage (cleared when the tab closes).
 * Never stored in localStorage. Health data is never stored here.
 * -------------------------------------------------------------- */

const ACCESS_KEY = "ve.access_token";
const REFRESH_KEY = "ve.refresh_token";

let accessToken: string | null = null;
let refreshToken: string | null = null;
let refreshPromise: Promise<void> | null = null;

/** Hydrate tokens from sessionStorage (browser only). */
export function hydrateTokens(): void {
  if (typeof window === "undefined") return;
  accessToken = window.sessionStorage.getItem(ACCESS_KEY);
  refreshToken = window.sessionStorage.getItem(REFRESH_KEY);
}

function persistTokens(): void {
  if (typeof window === "undefined") return;
  if (accessToken) window.sessionStorage.setItem(ACCESS_KEY, accessToken);
  else window.sessionStorage.removeItem(ACCESS_KEY);
  if (refreshToken) window.sessionStorage.setItem(REFRESH_KEY, refreshToken);
  else window.sessionStorage.removeItem(REFRESH_KEY);
}

export function setTokens(access: string, refresh: string): void {
  accessToken = access;
  refreshToken = refresh;
  persistTokens();
}

export function getAccessToken(): string | null {
  return accessToken;
}

export function getRefreshToken(): string | null {
  return refreshToken;
}

export function clearTokens(): void {
  accessToken = null;
  refreshToken = null;
  if (typeof window !== "undefined") {
    window.sessionStorage.removeItem(ACCESS_KEY);
    window.sessionStorage.removeItem(REFRESH_KEY);
  }
}

/* -------------------------------------------------------------- */
/* Core fetch wrapper                                              */
/* -------------------------------------------------------------- */

interface RequestOptions extends Omit<RequestInit, "body"> {
  body?: unknown;
  /** Skip Authorization header (for auth endpoints). */
  skipAuth?: boolean;
}

async function tryRefreshToken(): Promise<boolean> {
  if (!refreshToken) return false;

  try {
    const res = await fetch("/api/v1/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });

    if (!res.ok) {
      clearTokens();
      return false;
    }

    const data = await res.json();
    setTokens(data.accessToken, data.refreshToken);
    return true;
  } catch {
    clearTokens();
    return false;
  }
}

/**
 * Centralized API request function.
 * - Injects Authorization header when a token is available.
 * - Parses the standardized error response format.
 * - Attempts a single token refresh on 401.
 */
export async function apiRequest<T>(
  url: string,
  options: RequestOptions = {}
): Promise<T> {
  const { body, skipAuth, ...init } = options;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(init.headers as Record<string, string>),
  };

  if (!skipAuth && accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  }

  let res = await fetch(url, {
    ...init,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  /* Retry once with a refreshed token on 401. */
  if (res.status === 401 && !skipAuth && refreshToken) {
    /* Deduplicate concurrent refresh attempts. */
    if (!refreshPromise) {
      refreshPromise = tryRefreshToken().then((ok) => {
        refreshPromise = null;
        if (!ok) throw new ApiError(401, {
          error: { code: "AUTHENTICATION_ERROR", message: "Session expired. Please sign in again.", details: [] },
        });
      });
    }

    await refreshPromise;

    /* Retry the original request with the new token. */
    if (accessToken) {
      headers["Authorization"] = `Bearer ${accessToken}`;
      res = await fetch(url, {
        ...init,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
      });
    }
  }

  /* Parse response */
  if (!res.ok) {
    let errorBody: ErrorResponse;
    try {
      errorBody = await res.json();
    } catch {
      errorBody = {
        error: {
          code: "NETWORK_ERROR",
          message: "An unexpected error occurred. Please try again.",
          details: [],
        },
      };
    }
    throw new ApiError(res.status, errorBody);
  }

  /* Handle 204 No Content */
  if (res.status === 204) {
    return undefined as T;
  }

  return res.json() as Promise<T>;
}

/* -------------------------------------------------------------- */
/* Convenience methods                                             */
/* -------------------------------------------------------------- */

export const api = {
  get<T>(url: string, options?: RequestOptions): Promise<T> {
    return apiRequest<T>(url, { ...options, method: "GET" });
  },
  post<T>(url: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return apiRequest<T>(url, { ...options, method: "POST", body });
  },
  put<T>(url: string, body?: unknown, options?: RequestOptions): Promise<T> {
    return apiRequest<T>(url, { ...options, method: "PUT", body });
  },
  delete<T>(url: string, options?: RequestOptions): Promise<T> {
    return apiRequest<T>(url, { ...options, method: "DELETE" });
  },
};
