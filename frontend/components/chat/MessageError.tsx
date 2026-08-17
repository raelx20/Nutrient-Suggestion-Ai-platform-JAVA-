import styles from "./MessageError.module.css";

export interface MessageErrorProps {
  message: string;
  retryLabel?: string;
  onRetry?: () => void;
}

export function MessageError({
  message,
  retryLabel = "Try again",
  onRetry,
}: MessageErrorProps) {
  return (
    <div className={styles.row}>
      <div className={styles.box} role="alert">
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
        <p className={styles.message}>{message}</p>
        {onRetry && (
          <button type="button" className={styles.retry} onClick={onRetry}>
            {retryLabel}
          </button>
        )}
      </div>
    </div>
  );
}