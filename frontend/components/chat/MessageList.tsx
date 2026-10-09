"use client";

import { useEffect, useRef } from "react";
import styles from "./MessageList.module.css";

export interface MessageListProps {
  children: React.ReactNode;
}

/** Scrollable message area with auto-scroll-to-bottom. */
export function MessageList({ children }: MessageListProps) {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    const prefersReducedMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)"
    ).matches;
    el.scrollTo({
      top: el.scrollHeight,
      behavior: prefersReducedMotion ? "auto" : "smooth",
    });
  }, [children]);

  return (
    <div ref={containerRef} className={styles.list} aria-live="polite" role="log">
      <div className={styles.inner}>{children}</div>
    </div>
  );
}