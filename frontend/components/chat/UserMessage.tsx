import { formatTime } from "@/lib/utils";
import styles from "./Messages.module.css";

export interface UserMessageProps {
  content: string;
  createdAt?: string;
}

/** Right-aligned user message bubble. */
export function UserMessage({ content, createdAt }: UserMessageProps) {
  return (
    <div className={styles.userRow}>
      <div className={styles.userBubble}>{content}</div>
      {createdAt && (
        <span className={styles.timestamp}>{formatTime(createdAt)}</span>
      )}
    </div>
  );
}