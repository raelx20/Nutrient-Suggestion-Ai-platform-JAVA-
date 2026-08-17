/* ================================================================
 * API barrel export — single import point for all API modules.
 * ================================================================ */

export {
  api,
  ApiError,
  setTokens,
  getAccessToken,
  getRefreshToken,
  clearTokens,
  hydrateTokens,
} from "./client";
export { authApi } from "./auth";
export { chatApi } from "./chat";
export { assessmentsApi } from "./assessments";
export { productsApi } from "./products";
export { adminApi } from "./admin";
