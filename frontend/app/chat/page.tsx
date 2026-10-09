"use client";

import { useRouter } from "next/navigation";
import { useChat, type ChatItem } from "@/hooks";
import { consumerConfig } from "@/lib/config/consumer";
import { WelcomeState } from "@/components/consumer";
import {
  ChatComposer,
  MessageList,
  UserMessage,
  AssistantMessage,
  ChatLoadingIndicator,
  SuggestedActions,
  MessageError,
} from "@/components/chat";
import { AssessmentWarning, AssessmentForm } from "@/components/assessment";
import { RecommendationResult } from "@/components/products";
import { Button, LoadingSkeleton } from "@/components/common";
import type { AssessmentDetailResponse, MessageResponse, SuggestedAction } from "@/types";
import styles from "./page.module.css";

export default function ChatPage() {
  const router = useRouter();
  const chat = useChat();
  const { items, isInitialLoading } = chat;

  const handleAction = async (action: SuggestedAction) => {
    switch (action.type) {
      case "start_assessment":
        await chat.startAssessment();
        break;
      case "view_recommendations":
        await chat.sendMessage("Show me my recommendations");
        break;
      default:
        await chat.sendMessage(action.label);
    }
  };

  const contactCounsellor = () => {
    chat.sendMessage("I'd like to speak with a nutrition counsellor.");
  };

  if (isInitialLoading) {
    return (
      <div className={styles.loading} aria-live="polite">
        <LoadingSkeleton variant="text" width={240} height={20} />
        <LoadingSkeleton variant="text" width={420} height={16} />
        <LoadingSkeleton variant="text" width={320} height={16} />
      </div>
    );
  }

  /* ---- Initial welcome state ---- */
  if (items.length === 0) {
    return (
      <div className={styles.welcomeLayout}>
        <WelcomeState
          headline={consumerConfig.welcomeHeadline}
          subtext={consumerConfig.welcomeSubtext}
          action={
            <Button
              type="button"
              variant="outline"
              onClick={() => chat.startAssessment()}
            >
              Start your nutrition assessment
            </Button>
          }
        >
          <ChatComposer
            placeholder={consumerConfig.composerPlaceholder}
            autoFocus
            disabled={chat.isSending}
            onSubmit={chat.sendMessage}
          />
        </WelcomeState>
      </div>
    );
  }

  /* ---- Conversation ---- */
  return (
    <div className={styles.conversation}>
      <MessageList>
        {items.map((item, i) => (
          <ChatItemView
            key={`${item.type}-${i}`}
            item={item}
            chat={chat}
            onViewProduct={(id) => router.push(`/products/${id}`)}
            onContactCounsellor={contactCounsellor}
          />
        ))}
        {chat.isSending && <ChatLoadingIndicator />}
      </MessageList>

      <div className={styles.composerArea}>
        <SuggestedActions
          actions={chat.suggestedActions}
          disabled={chat.isSending}
          onSelect={handleAction}
        />
        <ChatComposer
          placeholder={consumerConfig.composerPlaceholder}
          disabled={chat.isSending}
          onSubmit={chat.sendMessage}
        />
      </div>
    </div>
  );
}

/* ---- Item renderer ---- */

interface ChatItemViewProps {
  item: ChatItem;
  chat: ReturnType<typeof useChat>;
  onViewProduct: (productId: string) => void;
  onContactCounsellor: () => void;
}

function ChatItemView({
  item,
  chat,
  onViewProduct,
  onContactCounsellor,
}: ChatItemViewProps) {
  switch (item.type) {
    case "message":
      return <MessageView message={item.message} />;

    case "loading":
      return <ChatLoadingIndicator />;

    case "error":
      return (
        <MessageError
          message={item.message}
          onRetry={
            item.retryable && item.text
              ? () => chat.retryMessage(item.id, item.text!)
              : undefined
          }
        />
      );

    case "assessment-warning":
      return (
        <AssessmentWarning
          title={consumerConfig.assessmentWarningTitle}
          body={consumerConfig.assessmentWarningBody}
          ctaLabel={consumerConfig.assessmentWarningCta}
          loading={chat.isSending}
          onContinue={chat.beginAssessmentFromWarning}
        />
      );

    case "assessment":
      return (
        <AssessmentForm
          data={item.data}
          onComplete={handleAssessmentComplete}
          onError={() => {
            /* Error is surfaced inside the form. */
          }}
        />
      );

    case "recommendation":
      return (
        <RecommendationResult
          data={item.data}
          onViewProduct={onViewProduct}
          onContactCounsellor={onContactCounsellor}
        />
      );

    default:
      return null;
  }

  function handleAssessmentComplete(detail: AssessmentDetailResponse) {
    chat.completeAssessment(detail);
  }
}

function MessageView({ message }: { message: MessageResponse }) {
  if (message.role === "user") {
    return <UserMessage content={message.content} createdAt={message.createdAt} />;
  }
  return (
    <AssistantMessage
      content={message.content}
      createdAt={message.createdAt}
      messageType={message.messageType}
    />
  );
}