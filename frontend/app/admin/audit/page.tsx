"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminAuditPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Audit Logs"
        description="Read-only record of administrative actions across the platform."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/audit-logs"
        description="A filterable, read-only audit table (timestamp, admin, action, resource, result) will render here once the backend exposes audit log endpoints."
      />
    </div>
  );
}