/* ================================================================
 * Admin API — typed contracts for admin-only endpoints.
 *
 * Most of these endpoints do NOT yet exist in the backend.
 * The types and functions define the expected contracts so the
 * frontend is ready when they're implemented.
 * ================================================================ */

import { api } from "./client";

const ADMIN = "/api/v1/admin";

/* -------------------------------------------------------------- */
/* Type contracts for future admin endpoints                       */
/* -------------------------------------------------------------- */

export interface AdminDashboardMetrics {
  totalUsers: number;
  activeUsers: number;
  completedAssessments: number;
  totalRecommendations: number;
  productSelections: number;
  counsellorReferrals: number;
  totalSales: number;
  conversionRate: number;
}

export interface AdminUserListParams {
  q?: string;
  page?: number;
  size?: number;
  status?: string;
  role?: string;
}

export interface AdminUserSummary {
  id: string;
  email: string;
  fullName: string | null;
  status: string;
  verified: boolean;
  active: boolean;
  roles: string[];
  createdAt: string;
  lastLoginAt: string | null;
}

export interface AdminPaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/* -------------------------------------------------------------- */
/* Admin API functions (awaiting backend implementation)            */
/* -------------------------------------------------------------- */

export const adminApi = {
  /** Get dashboard metrics. */
  getDashboardMetrics(): Promise<AdminDashboardMetrics> {
    return api.get(`${ADMIN}/dashboard/metrics`);
  },

  /** List users (paginated). */
  listUsers(
    params?: AdminUserListParams
  ): Promise<AdminPaginatedResponse<AdminUserSummary>> {
    const query = new URLSearchParams();
    if (params?.q) query.set("q", params.q);
    if (params?.page !== undefined) query.set("page", String(params.page));
    if (params?.size !== undefined) query.set("size", String(params.size));
    if (params?.status) query.set("status", params.status);
    if (params?.role) query.set("role", params.role);
    const qs = query.toString();
    return api.get(`${ADMIN}/users${qs ? `?${qs}` : ""}`);
  },

  /** Get single user detail. */
  getUser(id: string): Promise<AdminUserSummary> {
    return api.get(`${ADMIN}/users/${id}`);
  },

  /** Search across all admin resources. */
  globalSearch(
    q: string
  ): Promise<{ users: AdminUserSummary[]; products: unknown[] }> {
    return api.get(`${ADMIN}/search?q=${encodeURIComponent(q)}`);
  },
};
