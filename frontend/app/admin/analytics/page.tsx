"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminAnalyticsPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Analytics"
        description="User, assessment, recommendation, product, and AI analytics."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/analytics/*"
        description="Analytics sections (users, assessments, recommendations, products, AI) will render here once the backend exposes analytics endpoints."
      />
    </div>
  );
}