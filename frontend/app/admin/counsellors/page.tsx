"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminCounsellorsPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Counsellors"
        description="Manage nutrition counsellors: availability, contact, and referral volume."
      />
      <AwaitingBackend
        endpoint="GET/POST/PUT/DELETE /api/v1/admin/counsellors"
        description="Counsellor listing, add/edit/activate/deactivate, and referral volume will render here once the backend exposes counsellor endpoints."
      />
    </div>
  );
}