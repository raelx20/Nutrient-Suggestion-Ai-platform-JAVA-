import type { SuggestedAction } from "@/types";
import styles from "./SuggestedActions.module.css";

export interface SuggestedActionsProps {
  actions: SuggestedAction[];
  disabled?: boolean;
  onSelect: (action: SuggestedAction) => void;
}

/** Renders backend-provided suggested actions as interactive buttons. */
export function SuggestedActions({
  actions,
  disabled = false,
  onSelect,
}: SuggestedActionsProps) {
  if (!actions.length) return null;

  return (
    <div className={styles.actions} role="group" aria-label="Suggested actions">
      {actions.map((action, i) => (
        <button
          key={`${action.type}-${i}`}
          type="button"
          className={styles.action}
          disabled={disabled}
          onClick={() => onSelect(action)}
        >
          <span className={styles.actionIcon} aria-hidden="true">
            <svg
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="12" cy="12" r="10" />
              <path d="M12 16v-4M12 8h.01" />
            </svg>
          </span>
          {action.label}
        </button>
      ))}
    </div>
  );
}