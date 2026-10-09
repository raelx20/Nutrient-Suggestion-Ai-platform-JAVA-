"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../../admin-pages.module.css";

export default function AdminUserDetailPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="User detail"
        description="Personal, medical, lifestyle, assessment, and conversation history for a single user."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/users/{id}"
        description="An organized, admin-only profile (personal, body, medical, lifestyle, nutrition, assessment, conversation, recommendation, and audit sections) will render here once the backend user-detail endpoint is implemented."
      />
    </div>
  );
}