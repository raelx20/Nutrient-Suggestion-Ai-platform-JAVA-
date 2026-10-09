/* ================================================================
 * Error DTOs — mirrors Spring Boot ErrorResponse.
 * ================================================================ */

export interface ErrorDetail {
  field: string;
  message: string;
  code: string;
}

export interface ErrorBody {
  code: string;
  message: string;
  details: ErrorDetail[];
}

export interface ErrorResponse {
  error: ErrorBody;
}
