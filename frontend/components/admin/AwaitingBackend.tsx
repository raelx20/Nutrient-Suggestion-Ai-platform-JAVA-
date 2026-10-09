import { EmptyState } from "@/components/common";
import styles from "./AwaitingBackend.module.css";

export interface AwaitingBackendProps {
  /** Endpoint that this page depends on. */
  endpoint: string;
  /** Human description of what will render here. */
  description?: string;
}

/**
 * Honest "awaiting backend" state for admin pages whose endpoints
 * are not yet implemented in the Spring Boot API. No fake data.
 */
export function AwaitingBackend({ endpoint, description }: AwaitingBackendProps) {
  return (
    <div className={styles.wrapper}>
      <EmptyState
        icon={
          <svg
            width="28"
            height="28"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M10.3 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.7 3.86a2 2 0 0 0-3.4 0z" />
            <line x1="12" y1="9" x2="12" y2="13" />
            <line x1="12" y1="17" x2="12.01" y2="17" />
          </svg>
        }
        title="Awaiting backend endpoint"
        description={
          description ??
          "This section is ready in the frontend. It will display live data once the backend exposes the required endpoint."
        }
        action={
          <code className={styles.endpoint} aria-label="Required endpoint">
            {endpoint}
          </code>
        }
      />
    </div>
  );
}