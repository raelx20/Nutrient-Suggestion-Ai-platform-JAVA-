"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminUsersPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Users"
        description="Manage consumers, counsellors, and administrators."
        actions={
          <span className={styles.pill}>Admin only</span>
        }
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/users"
        description="A searchable, filterable user table (with per-user detail) will render here once the backend exposes the users endpoints. Typed contracts are defined in lib/api/admin.ts."
      />
    </div>
  );
}