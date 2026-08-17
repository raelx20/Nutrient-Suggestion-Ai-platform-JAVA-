import styles from "./Card.module.css";

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  elevated?: boolean;
  hoverable?: boolean;
  padding?: "none" | "sm" | "md" | "lg";
  children: React.ReactNode;
}

export function Card({
  elevated = false,
  hoverable = false,
  padding = "md",
  className,
  children,
  ...props
}: CardProps) {
  const classes = [
    styles.card,
    elevated ? styles.elevated : "",
    hoverable ? styles.hoverable : "",
    styles[`padding-${padding}`],
    className ?? "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <div className={classes} {...props}>
      {children}
    </div>
  );
}
