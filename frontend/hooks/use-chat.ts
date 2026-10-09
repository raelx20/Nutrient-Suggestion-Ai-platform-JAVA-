/* ================================================================
 * useChat — manages conversation state, message sending, loading,
 * error handling, conversation restoration, and the inline
 * assessment/recommendation flow.
 *
 * The backend owns conversation state; the frontend renders it.
 * ================================================================ */

"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { chatApi, assessmentsApi, ApiError } from "@/lib/api";
import type {
  ChatRequest,
  ChatResponse,
  ConversationResponse,
  MessageResponse,
  SessionType,
  SuggestedAction,
  StartAssessmentResponse,
  AssessmentDetailResponse,
} from "@/types";

/** Items rendered inside the conversation. */
export type ChatItem =
  | { type: "message"; message: MessageResponse }
  | { type: "loading" }
  | { type: "error"; id: string; message: string; text?: string; retryable?: boolean }
  | { type: "assessment-warning" }
  | { type: "assessment"; data: StartAssessmentResponse }
  | { type: "recommendation"; data: AssessmentDetailResponse };

export interface UseChatResult {
  items: ChatItem[];
  conversationId: string | null;
  sessionType: SessionType | null;
  suggestedActions: SuggestedAction[];
  isInitialLoading: boolean;
  isSending: boolean;
  error: string | null;

  sendMessage: (text: string) => Promise<void>;
  retryMessage: (itemId: string, text: string) => Promise<void>;
  startAssessment: () => Promise<void>;
  beginAssessmentFromWarning: () => Promise<void>;
  completeAssessment: (detail: AssessmentDetailResponse) => void;
  dismissError: () => void;
  clearConversation: () => void;
}

export function useChat(): UseChatResult {
  const [items, setItems] = useState<ChatItem[]>([]);
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [sessionType, setSessionType] = useState<SessionType | null>(null);
  const [suggestedActions, setSuggestedActions] = useState<SuggestedAction[]>(
    []
  );
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const conversationIdRef = useRef<string | null>(null);
  const itemsRef = useRef<ChatItem[]>([]);
  const pendingIdRef = useRef(0);

  useEffect(() => {
    conversationIdRef.current = conversationId;
  }, [conversationId]);

  useEffect(() => {
    itemsRef.current = items;
  }, [items]);

  const nextPendingId = () => `pending-${++pendingIdRef.current}`;

  /* ---- Conversation restoration on mount ---- */
  useEffect(() => {
    let cancelled = false;

    async function restore() {
      try {
        const conversations: ConversationResponse[] =
          await chatApi.listConversations();
        if (cancelled) return;

        const active = conversations.find(
          (c) =>
            c.status === "active" || c.status === "awaiting_assessment"
        );
        if (!active) {
          setConversationId(null);
          setIsInitialLoading(false);
          return;
        }

        const detail = await chatApi.getConversation(active.id);
        if (cancelled) return;

        const restoredItems: ChatItem[] = detail.messages.map((m) => ({
          type: "message" as const,
          message: m,
        }));
        setItems(restoredItems);
        setConversationId(detail.id);
        setSessionType(detail.sessionType);
      } catch {
        /* Backend unreachable — start fresh. */
        if (!cancelled) setError("Unable to connect. Please try again.");
      } finally {
        if (!cancelled) setIsInitialLoading(false);
      }
    }

    restore();
    return () => {
      cancelled = true;
    };
  }, []);

  /* ---- Core send ---- */
  const sendMessage = useCallback(async (text: string) => {
    const trimmed = text.trim();
    if (!trimmed || isSending) return;

    setError(null);
    setIsSending(true);

    /* Build the chat request. The backend drives session behavior;
       the frontend only suggests the starting session type. */
    const request: ChatRequest = { message: trimmed };
    const hasConversation = !!conversationIdRef.current;
    const hasItems = itemsRef.current.length > 0;
    if (!hasConversation && !hasItems) {
      request.sessionType = "welcome";
    }

    /* Optimistically show the user message. */
    const optimistic: MessageResponse = {
      id: nextPendingId(),
      role: "user",
      content: trimmed,
      messageType: "text",
      createdAt: new Date().toISOString(),
    };
    const optimisticIndex = itemsRef.current.length;
    setItems((prev) => [...prev, { type: "message", message: optimistic }]);

    try {
      const res: ChatResponse = await chatApi.sendMessage(
        request,
        conversationIdRef.current ?? undefined
      );

      setItems((prev) => {
        const next = [...prev];
        /* Replace the optimistic user message with the authoritative one. */
        next[optimisticIndex] = { type: "message", message: res.userMessage };
        next.push({ type: "message", message: res.assistantMessage });
        return next;
      });
      setConversationId(res.conversationId);
      setSessionType(res.sessionType);
      setSuggestedActions(res.suggestedActions);
    } catch (err) {
      setItems((prev) => {
        const next = [...prev];
        next[optimisticIndex] = {
          type: "error",
          id: nextPendingId(),
          message: friendlyChatError(err),
          text: trimmed,
          retryable: true,
        };
        return next;
      });
      setError(friendlyChatError(err));
    } finally {
      setIsSending(false);
    }
  }, [isSending]);

  /* ---- Retry a failed message ---- */
  const retryMessage = useCallback(
    async (itemId: string, text: string) => {
      setItems((prev) => prev.filter((i) => !(i.type === "error" && i.id === itemId)));
      await sendMessage(text);
    },
    [sendMessage]
  );

  /* ---- Assessment entry (from suggested action or welcome screen) ---- */
  const beginAssessmentFromWarning = useCallback(async () => {
    setError(null);
    setIsSending(true);
    try {
      const data = await assessmentsApi.start();
      setItems((prev) => [...prev, { type: "assessment", data }]);
    } catch (err) {
      setItems((prev) => [
        ...prev,
        {
          type: "error",
          id: nextPendingId(),
          message: friendlyChatError(err),
          retryable: true,
        },
      ]);
      setError(friendlyChatError(err));
    } finally {
      setIsSending(false);
    }
  }, []);

  const startAssessment = useCallback(async () => {
    /* Show the non-alarming warning first (config-driven copy). */
    setItems((prev) => [...prev, { type: "assessment-warning" }]);
  }, []);

  /** Called by the assessment form once the backend returns results. */
  const completeAssessment = useCallback(
    (detail: AssessmentDetailResponse) => {
      setItems((prev) => [...prev, { type: "recommendation", data: detail }]);
      setSuggestedActions([]);
    },
    []
  );

  const dismissError = useCallback(() => setError(null), []);
  const clearConversation = useCallback(() => {
    setItems([]);
    setConversationId(null);
    setSessionType(null);
    setSuggestedActions([]);
    setError(null);
  }, []);

  return {
    items,
    conversationId,
    sessionType,
    suggestedActions,
    isInitialLoading,
    isSending,
    error,
    sendMessage,
    retryMessage,
    startAssessment,
    beginAssessmentFromWarning,
    completeAssessment,
    dismissError,
    clearConversation,
  };
}

/* ---- Helpers ---- */

function friendlyChatError(err: unknown): string {
  if (err instanceof ApiError) {
    if (err.status === 429)
      return "You're sending messages a little too quickly. Please wait a moment and try again.";
    if (err.status === 401)
      return "Your session has expired. Please sign in again.";
    return err.message;
  }
  return "Unable to connect. Please try again.";
}