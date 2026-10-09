import styles from "./AssessmentWarning.module.css";

export interface AssessmentWarningProps {
  title?: string;
  body?: string;
  ctaLabel?: string;
  loading?: boolean;
  onContinue: () => void;
}

/** Pre-assessment, non-alarming guidance card. */
export function AssessmentWarning({
  title = "A quick assessment",
  body,
  ctaLabel = "Continue",
  loading = false,
  onContinue,
}: AssessmentWarningProps) {
  return (
    <div className={styles.warning}>
      <div className={styles.icon} aria-hidden="true">
        <svg
          width="22"
          height="22"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M12 8v4M12 16h.01" />
          <path d="M10.3 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.7 3.86a2 2 0 0 0-3.4 0z" />
        </svg>
      </div>
      <h3 className={styles.title}>{title}</h3>
      <p className={styles.body}>
        {body ??
          "These questions are important for creating your personalized nutrition assessment. Please answer honestly and as accurately as possible. If you are unsure about an answer, or if you have a complex medical situation, please contact a counsellor."}
      </p>
      <button
        type="button"
        className={styles.continue}
        onClick={onContinue}
        disabled={loading}
      >
        {loading ? "Starting…" : ctaLabel}
      </button>
    </div>
  );
}