"use client";

import Link from "next/link";
import { ProfileMenu } from "./ProfileMenu";
import { consumerConfig } from "@/lib/config/consumer";
import styles from "./ConsumerHeader.module.css";

export function ConsumerHeader() {
  return (
    <header className={styles.header}>
      <Link href="/chat" className={styles.logo} aria-label="VitalEdge home">
        <span className={styles.logoIcon} aria-hidden="true">
          V
        </span>
        <span className={styles.logoText}>{consumerConfig.appName}</span>
      </Link>

      <div className={styles.actions}>
        <ProfileMenu />
      </div>
    </header>
  );
}