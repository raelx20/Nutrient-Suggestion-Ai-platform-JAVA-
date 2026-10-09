/* ================================================================
 * Chat API — typed wrappers for /api/v1/chat endpoints.
 * ================================================================ */

import { api } from "./client";
import type {
  ChatRequest,
  ChatResponse,
  ConversationResponse,
} from "@/types";

const BASE = "/api/v1/chat";

export const chatApi = {
  /** Send a message (optionally within an existing conversation). */
  sendMessage(
    data: ChatRequest,
    conversationId?: string
  ): Promise<ChatResponse> {
    const url = conversationId
      ? `${BASE}?conversationId=${conversationId}`
      : BASE;
    return api.post(url, data);
  },

  /** List all conversations for the current user. */
  listConversations(): Promise<ConversationResponse[]> {
    return api.get(`${BASE}/conversations`);
  },

  /** Get a specific conversation with messages. */
  getConversation(id: string): Promise<ConversationResponse> {
    return api.get(`${BASE}/conversations/${id}`);
  },
};
