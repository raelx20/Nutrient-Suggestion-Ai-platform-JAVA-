import styles from "./WelcomeState.module.css";

export interface WelcomeStateProps {
  /** Large centered headline. */
  headline: string;
  /** Supporting copy under the headline. */
  subtext?: string;
  /** Optional accent action (e.g. "Start assessment"). */
  action?: React.ReactNode;
  /** Composer slot. */
  children?: React.ReactNode;
}

/**
 * ChatGPT-style initial screen: large centered welcome message with a
 * prominent composer. The exact copy is configurable via consumerConfig.
 */
export function WelcomeState({
  headline,
  subtext,
  action,
  children,
}: WelcomeStateProps) {
  return (
    <div className={styles.wrapper}>
      <div className={styles.content}>
        <h1 className={styles.headline}>{headline}</h1>
        {subtext && <p className={styles.subtext}>{subtext}</p>}
        {action && <div className={styles.action}>{action}</div>}
        {children && <div className={styles.composer}>{children}</div>}
      </div>
    </div>
  );
}