"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminRecommendationsPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Recommendations"
        description="Review recommendations, acceptance, alternatives, and counsellor referrals."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/recommendations"
        description="A recommendation list (product, user, acceptance state, date) will render here once the backend exposes recommendation listing."
      />
    </div>
  );
}