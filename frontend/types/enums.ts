/* ================================================================
 * Backend enum mirrors — keep in sync with Java domain enums.
 * ================================================================ */

export type RoleType = "consumer" | "counsellor" | "admin";

export type UserStatus = "pending" | "verified" | "locked";

export type SessionType =
  | "welcome"
  | "general"
  | "assessment"
  | "review"
  | "recommendation"
  | "post_recommendation";

export type MessageType =
  | "text"
  | "assessment_result"
  | "recommendation"
  | "summary"
  | "system";

export type MessageRole = "user" | "assistant" | "counsellor" | "system";

export type ConversationStatus =
  | "active"
  | "awaiting_assessment"
  | "completed"
  | "archived";

export type AssessmentStatus = "in_progress" | "completed" | "abandoned";

export type ProductStatus = "DRAFT" | "ACTIVE" | "INACTIVE" | "ARCHIVED";

export type ProductCategory = "low" | "medium" | "high";

export type AgeGroup = "children" | "adults" | "seniors" | "all";
