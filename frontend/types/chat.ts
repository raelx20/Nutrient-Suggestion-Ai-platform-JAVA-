/* ================================================================
 * Chat/Conversation DTOs — mirrors Spring Boot conversation records.
 * ================================================================ */

import type {
  ConversationStatus,
  MessageRole,
  MessageType,
  SessionType,
} from "./enums";

/** POST /api/v1/chat */
export interface ChatRequest {
  message: string;
  sessionType?: string;
}

/** Action suggested by the AI in a chat response */
export interface SuggestedAction {
  type: string;
  label: string;
  payload: unknown;
}

/** Response from POST /api/v1/chat */
export interface ChatResponse {
  conversationId: string;
  userMessage: MessageResponse;
  assistantMessage: MessageResponse;
  sessionType: SessionType;
  suggestedActions: SuggestedAction[];
}

/** Single message within a conversation */
export interface MessageResponse {
  id: string;
  role: MessageRole;
  content: string;
  messageType: MessageType;
  createdAt: string;
}

/** Full conversation with messages */
export interface ConversationResponse {
  id: string;
  status: ConversationStatus;
  sessionType: SessionType;
  startedAt: string;
  updatedAt: string;
  messages: MessageResponse[];
}
