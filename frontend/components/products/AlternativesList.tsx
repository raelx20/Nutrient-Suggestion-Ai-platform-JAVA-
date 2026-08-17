import type { RecommendationResponse } from "@/types";
import styles from "./AlternativesList.module.css";

export interface AlternativesListProps {
  alternatives: RecommendationResponse[];
  onSelect?: (productId: string) => void;
  onContactCounsellor?: () => void;
}

/** Alternatives shown when the user rejects the primary recommendation. */
export function AlternativesList({
  alternatives,
  onSelect,
  onContactCounsellor,
}: AlternativesListProps) {
  if (alternatives.length === 0) return null;

  return (
    <div className={styles.wrapper}>
      <h4 className={styles.title}>Here are some other options:</h4>
      <div className={styles.list}>
        {alternatives.map((alt) => (
          <div key={alt.id} className={styles.item}>
            <div className={styles.itemBody}>
              <h5 className={styles.itemName}>{alt.productName}</h5>
              <p className={styles.itemReason}>
                {alt.reason || "Supports: general nutritional support"}
              </p>
            </div>
            {onSelect && (
              <button
                type="button"
                className={styles.itemButton}
                onClick={() => onSelect(alt.productId)}
              >
                View
              </button>
            )}
          </div>
        ))}
      </div>

      {onContactCounsellor && (
        <div className={styles.footer}>
          <span className={styles.footerText}>Need more help?</span>
          <button
            type="button"
            className={styles.counsellorButton}
            onClick={onContactCounsellor}
          >
            Talk to a Counsellor
          </button>
        </div>
      )}
    </div>
  );
}