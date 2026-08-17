"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../../admin-pages.module.css";

export default function AdminConversationDetailPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Conversation detail"
        description="Read-only conversation transcript with assessment transitions and recommendation events."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/conversations/{id}"
        description="A read-only transcript (user/AI messages, assessment transition, recommendation, counsellor referral) will render here once the backend detail endpoint is implemented."
      />
    </div>
  );
}