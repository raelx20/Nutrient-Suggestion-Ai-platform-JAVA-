"use client";

import { useState } from "react";
import styles from "./FilterPanel.module.css";

export interface FilterPanelProps {
  title?: string;
  children: React.ReactNode;
  onReset?: () => void;
}

/** Collapsible filter controls for admin list pages. */
export function FilterPanel({ title = "Filters", children, onReset }: FilterPanelProps) {
  const [open, setOpen] = useState(false);

  return (
    <div className={styles.panel}>
      <div className={styles.header}>
        <button
          type="button"
          className={styles.toggle}
          onClick={() => setOpen((v) => !v)}
          aria-expanded={open}
        >
          <span
            className={`${styles.chevron} ${open ? styles.chevronOpen : ""}`}
            aria-hidden="true"
          >
            ▸
          </span>
          {title}
        </button>
        {onReset && (
          <button type="button" className={styles.reset} onClick={onReset}>
            Reset
          </button>
        )}
      </div>
      {open && <div className={styles.body}>{children}</div>}
    </div>
  );
}