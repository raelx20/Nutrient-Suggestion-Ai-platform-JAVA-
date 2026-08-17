"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminSettingsPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Settings"
        description="Application, questionnaire, welcome message, assessment warning, counsellor, product, AI, and notification configuration."
      />
      <AwaitingBackend
        endpoint="GET/PUT /api/v1/admin/settings"
        description="Configuration sections will render here once the backend exposes settings endpoints. Only backend-supported settings will be editable."
      />
    </div>
  );
}