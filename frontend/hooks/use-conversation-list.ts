/* ================================================================
 * useConversationList — loads the user's conversation list with
 * loading/error state.
 * ================================================================ */

"use client";

import { useCallback, useEffect, useState } from "react";
import { chatApi, ApiError } from "@/lib/api";
import type { ConversationResponse } from "@/types";

export interface UseConversationListResult {
  conversations: ConversationResponse[];
  isLoading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useConversationList(): UseConversationListResult {
  const [conversations, setConversations] = useState<ConversationResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const list = await chatApi.listConversations();
      setConversations(list);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Unable to connect. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const list = await chatApi.listConversations();
        if (!cancelled) setConversations(list);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof ApiError
              ? err.message
              : "Unable to connect. Please try again."
          );
        }
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  return { conversations, isLoading, error, refresh };
}