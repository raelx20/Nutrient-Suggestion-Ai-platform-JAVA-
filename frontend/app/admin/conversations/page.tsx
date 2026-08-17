"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminConversationsPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Conversations"
        description="Inspect user conversations and their assessment/recommendation state. Privacy-restricted."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/conversations"
        description="A conversation list with user, status, timestamps, and assessment/recommendation state will render here once the backend exposes conversation listing."
      />
    </div>
  );
}