import styles from "./AssessmentProgress.module.css";

export interface AssessmentProgressProps {
  answered: number;
  total: number;
  /** Backend-computed progress percent (0-100). */
  progressPercent: number;
}

/** Subtle progress bar driven by the backend's progressPercent. */
export function AssessmentProgress({
  answered,
  total,
  progressPercent,
}: AssessmentProgressProps) {
  const clamped = Math.max(0, Math.min(100, progressPercent));

  return (
    <div className={styles.progress} aria-label="Assessment progress">
      <div className={styles.meta}>
        <span className={styles.label}>Assessment</span>
        <span className={styles.percent}>{clamped}%</span>
      </div>
      <div className={styles.track} role="progressbar" aria-valuenow={clamped} aria-valuemin={0} aria-valuemax={100}>
        <div className={styles.fill} style={{ width: `${clamped}%` }} />
      </div>
      <p className={styles.caption}>
        {answered} of {total} questions answered
      </p>
    </div>
  );
}