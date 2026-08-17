import styles from "./ChatLoadingIndicator.module.css";

/** Typing indicator shown while the assistant is "thinking". */
export function ChatLoadingIndicator() {
  return (
    <div className={styles.row}>
      <div className={styles.bubble} role="status" aria-label="Assistant is typing">
        <span className={styles.dot} />
        <span className={styles.dot} />
        <span className={styles.dot} />
      </div>
    </div>
  );
}