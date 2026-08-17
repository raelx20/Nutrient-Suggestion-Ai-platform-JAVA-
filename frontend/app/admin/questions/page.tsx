"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminQuestionsPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Questions"
        description="Manage assessment questionnaires, questions, options, and validation rules."
      />
      <AwaitingBackend
        endpoint="GET/POST/PUT/DELETE /api/v1/admin/questions"
        description="Questionnaire management (list, create, edit, reorder, activate/deactivate) will render here once the backend exposes question endpoints."
      />
    </div>
  );
}