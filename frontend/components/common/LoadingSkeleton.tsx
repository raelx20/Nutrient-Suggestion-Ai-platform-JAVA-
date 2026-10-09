import styles from "./LoadingSkeleton.module.css";

export interface LoadingSkeletonProps {
  variant?: "text" | "heading" | "circle" | "card";
  width?: string | number;
  height?: string | number;
  className?: string;
}

export function LoadingSkeleton({
  variant = "text",
  width,
  height,
  className,
}: LoadingSkeletonProps) {
  const classes = [
    styles.skeleton,
    styles[variant],
    className ?? "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div
      className={classes}
      style={{ width, height }}
      role="status"
      aria-label="Loading"
    >
      <span className="sr-only">Loading…</span>
    </div>
  );
}
