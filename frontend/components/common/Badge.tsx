import styles from "./Badge.module.css";

export interface BadgeProps {
  variant?: "success" | "warning" | "error" | "info" | "neutral";
  dot?: boolean;
  children: React.ReactNode;
}

export function Badge({
  variant = "neutral",
  dot = false,
  children,
}: BadgeProps) {
  return (
    <span className={`${styles.badge} ${styles[variant]}`}>
      {dot && <span className={styles.dot} aria-hidden="true" />}
      {children}
    </span>
  );
}
