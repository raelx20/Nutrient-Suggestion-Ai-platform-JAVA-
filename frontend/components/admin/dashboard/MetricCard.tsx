import styles from "./MetricCard.module.css";

export interface MetricCardProps {
  label: string;
  value: string;
  /** e.g. "+8.4%" or "—" */
  trend?: string;
  trendPositive?: boolean;
  icon?: React.ReactNode;
}

export function MetricCard({ label, value, trend, trendPositive, icon }: MetricCardProps) {
  return (
    <div className={styles.card}>
      <div className={styles.top}>
        <span className={styles.label}>{label}</span>
        {icon && <span className={styles.icon}>{icon}</span>}
      </div>
      <p className={styles.value}>{value}</p>
      {trend && (
        <p
          className={`${styles.trend} ${
            trendPositive === undefined ? "" : trendPositive ? styles.up : styles.down
          }`}
        >
          {trend}
        </p>
      )}
    </div>
  );
}