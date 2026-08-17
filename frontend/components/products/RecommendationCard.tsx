import type { RecommendationResponse } from "@/types";
import styles from "./RecommendationCard.module.css";

export interface RecommendationCardProps {
  recommendation: RecommendationResponse;
  onViewProduct?: (productId: string) => void;
  onShowAlternatives?: () => void;
}

/**
 * Polished, consumer-friendly recommendation card.
 * Internal fields (confidence, scoreComponents, rank, excluded) are
 * never rendered to consumers.
 */
export function RecommendationCard({
  recommendation,
  onViewProduct,
  onShowAlternatives,
}: RecommendationCardProps) {
  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <span className={styles.eyebrow}>Recommended for you</span>
        {recommendation.safe && (
          <span className={styles.safe} role="note">
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            Suitable for you
          </span>
        )}
      </div>

      <h3 className={styles.name}>{recommendation.productName}</h3>

      <div className={styles.supports}>
        <span className={styles.supportsLabel}>Supports:</span>
        <span className={styles.supportsValue}>
          {recommendation.reason || "General nutritional support"}
        </span>
      </div>

      {recommendation.reason && (
        <div className={styles.why}>
          <span className={styles.whyLabel}>Why it may be suitable:</span>
          <p className={styles.whyText}>{recommendation.reason}</p>
        </div>
      )}

      <div className={styles.actions}>
        {onViewProduct && (
          <button
            type="button"
            className={styles.viewButton}
            onClick={() => onViewProduct(recommendation.productId)}
          >
            View Product
          </button>
        )}
        {onShowAlternatives && (
          <button
            type="button"
            className={styles.alternativesButton}
            onClick={onShowAlternatives}
          >
            Not what you&apos;re looking for?
          </button>
        )}
      </div>
    </div>
  );
}