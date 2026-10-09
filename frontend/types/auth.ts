/* ================================================================
 * Auth DTOs — mirrors Spring Boot auth request/response records.
 * ================================================================ */

import type { RoleType, UserStatus } from "./enums";

/** POST /api/v1/auth/register */
export interface RegisterRequest {
  email: string;
  password: string;
  fullName?: string;
}

/** POST /api/v1/auth/login */
export interface LoginRequest {
  email: string;
  password: string;
}

/** POST /api/v1/auth/verify-email */
export interface VerifyEmailRequest {
  token: string;
}

/** POST /api/v1/auth/refresh, POST /api/v1/auth/logout */
export interface RefreshTokenRequest {
  refreshToken: string;
}

/** Returned by login and refresh */
export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserResponse;
}

/** Returned by /auth/me and embedded in TokenResponse */
export interface UserResponse {
  id: string;
  email: string;
  fullName: string | null;
  status: UserStatus | null;
  verified: boolean;
  active: boolean;
  roles: RoleType[];
  createdAt: string;
}

/** Registration result */
export interface RegisterResponse {
  user: UserResponse;
  verification_token: string;
}
