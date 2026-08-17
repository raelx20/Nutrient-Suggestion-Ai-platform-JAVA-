/* ================================================================
 * Auth API — typed wrappers for /api/v1/auth endpoints.
 * ================================================================ */

import { api, setTokens, clearTokens, getRefreshToken } from "./client";
import type {
  LoginRequest,
  RefreshTokenRequest,
  RegisterRequest,
  RegisterResponse,
  TokenResponse,
  UserResponse,
  VerifyEmailRequest,
} from "@/types";

const BASE = "/api/v1/auth";

export const authApi = {
  /** Register a new consumer account. */
  register(data: RegisterRequest): Promise<RegisterResponse> {
    return api.post(`${BASE}/register`, data, { skipAuth: true });
  },

  /** Verify email with token. */
  verifyEmail(data: VerifyEmailRequest): Promise<{ message: string }> {
    return api.post(`${BASE}/verify-email`, data, { skipAuth: true });
  },

  /** Login and store tokens. */
  async login(data: LoginRequest): Promise<TokenResponse> {
    const res = await api.post<TokenResponse>(`${BASE}/login`, data, {
      skipAuth: true,
    });
    setTokens(res.accessToken, res.refreshToken);
    return res;
  },

  /** Refresh tokens with the current refresh token. */
  async refresh(data: RefreshTokenRequest): Promise<TokenResponse> {
    const res = await api.post<TokenResponse>(`${BASE}/refresh`, data, {
      skipAuth: true,
    });
    setTokens(res.accessToken, res.refreshToken);
    return res;
  },

  /** Get current user profile. */
  me(): Promise<UserResponse> {
    return api.get(`${BASE}/me`);
  },

  /** Logout — blacklist tokens. */
  async logout(): Promise<void> {
    try {
      const refresh = getRefreshToken();
      await api.post(
        `${BASE}/logout`,
        refresh ? { refreshToken: refresh } : undefined
      );
    } finally {
      clearTokens();
    }
  },
};
