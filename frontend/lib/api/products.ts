/* ================================================================
 * Products API — typed wrappers for product endpoints.
 * ================================================================ */

import { api } from "./client";
import type { ProductRequest, ProductResponse } from "@/types";

const PUBLIC = "/api/v1/products";
const ADMIN = "/api/v1/admin/products";

export interface ProductListParams {
  category?: string;
  age_group?: string;
  q?: string;
  page?: number;
  size?: number;
}

export const productsApi = {
  /* ---- Public (consumer) ---- */

  /** List active products with optional filters. */
  list(params?: ProductListParams): Promise<ProductResponse[]> {
    const query = new URLSearchParams();
    if (params?.category) query.set("category", params.category);
    if (params?.age_group) query.set("age_group", params.age_group);
    if (params?.q) query.set("q", params.q);
    if (params?.page !== undefined) query.set("page", String(params.page));
    if (params?.size !== undefined) query.set("size", String(params.size));
    const qs = query.toString();
    return api.get(`${PUBLIC}${qs ? `?${qs}` : ""}`);
  },

  /** Get a single product detail. */
  detail(id: string): Promise<ProductResponse> {
    return api.get(`${PUBLIC}/${id}`);
  },

  /* ---- Admin ---- */

  /** Create a product (admin only). */
  create(data: ProductRequest): Promise<ProductResponse> {
    return api.post(ADMIN, data);
  },

  /** Update a product (admin only). */
  update(id: string, data: ProductRequest): Promise<ProductResponse> {
    return api.put(`${ADMIN}/${id}`, data);
  },

  /** Delete a product (admin only). */
  delete(id: string): Promise<{ message: string }> {
    return api.delete(`${ADMIN}/${id}`);
  },
};
