import type { ProductResponse } from "@/types";
import { humanize } from "@/lib/utils";
import styles from "./ProductCard.module.css";

export interface ProductCardProps {
  product: ProductResponse;
  onSelect?: (productId: string) => void;
}

/** Clean consumer-facing product display. */
export function ProductCard({ product, onSelect }: ProductCardProps) {
  return (
    <div className={styles.card}>
      <div className={styles.top}>
        <span className={styles.badge}>{humanize(product.category ?? "")}</span>
        {product.ageGroup && (
          <span className={styles.age}>{humanize(product.ageGroup)}</span>
        )}
      </div>

      <h3 className={styles.name}>{product.name}</h3>
      {product.description && (
        <p className={styles.description}>{product.description}</p>
      )}

      {product.dietaryTags.length > 0 && (
        <div className={styles.tags}>
          {product.dietaryTags.map((tag) => (
            <span key={tag} className={styles.tag}>
              {humanize(tag)}
            </span>
          ))}
        </div>
      )}

      {onSelect && (
        <button
          type="button"
          className={styles.selectButton}
          onClick={() => onSelect(product.id)}
        >
          View Product
        </button>
      )}
    </div>
  );
}