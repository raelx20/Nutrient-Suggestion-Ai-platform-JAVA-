"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminAssessmentsPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Assessments"
        description="Review assessment completion, progress, BMI, goals, and recommendation status."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/assessments"
        description="A filterable assessment table (status, progress, BMI, primary goal, dates) will render here once the backend exposes assessment listing."
      />
    </div>
  );
}