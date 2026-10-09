/* ================================================================
 * Product DTOs — mirrors Spring Boot product records.
 * ================================================================ */

import type { AgeGroup, ProductCategory, ProductStatus } from "./enums";

/** Request body for creating/updating products (admin) */
export interface ProductRequest {
  name: string;
  sku: string;
  categoryCode: string;
  category: string;
  ageGroup: string;
  status?: string;
  description?: string;
  allergens?: string[];
  dietaryTags?: string[];
  nutritionalInfo?: Record<string, unknown>;
}

/** Product response from backend */
export interface ProductResponse {
  id: string;
  name: string;
  sku: string;
  category: ProductCategory | null;
  categoryCode: string;
  ageGroup: AgeGroup | null;
  status: ProductStatus | null;
  description: string | null;
  allergens: string[];
  dietaryTags: string[];
  nutritionalInfo: Record<string, unknown> | null;
  createdAt: string;
  updatedAt: string;
}
