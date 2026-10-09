import styles from "./DashboardGrid.module.css";

export interface DashboardGridProps {
  children: React.ReactNode;
}

/** Responsive metric card grid (2–4 columns by viewport). */
export function DashboardGrid({ children }: DashboardGridProps) {
  return <div className={styles.grid}>{children}</div>;
}