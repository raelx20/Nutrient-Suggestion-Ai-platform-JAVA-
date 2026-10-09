import styles from "./CounsellorCard.module.css";

export interface CounsellorCardProps {
  onContact?: () => void;
}

/**
 * Counsellor panel — shown when the backend reports
 * healthProfile.counsellorRecommended = true.
 * Only backend-provided information is rendered.
 */
export function CounsellorCard({ onContact }: CounsellorCardProps) {
  return (
    <div className={styles.card} role="note">
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
          <circle cx="12" cy="8" r="4" />
          <path d="M20 21a8 8 0 1 0-16 0" />
        </svg>
      </div>
      <h4 className={styles.title}>Talk to a nutrition counsellor</h4>
      <p className={styles.body}>
        It may be better to speak with a nutrition counsellor before selecting a
        product. They can help you understand your results and choose what is
        right for you.
      </p>
      {onContact && (
        <button type="button" className={styles.contact} onClick={onContact}>
          Contact a Counsellor
        </button>
      )}
    </div>
  );
}