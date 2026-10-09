"use client";

import { RouteGuard } from "@/lib/permissions/route-guard";
import { ConsumerHeader } from "@/components/consumer";
import styles from "./chat.module.css";

export default function ChatLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <RouteGuard requireAuth loginRedirect="/login">
      <div className={styles.shell}>
        <ConsumerHeader />
        <main className={styles.main}>{children}</main>
      </div>
    </RouteGuard>
  );
}