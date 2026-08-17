"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminSalesPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Sales"
        description="Sales metrics, orders, revenue, conversion, and product performance with daily/weekly/monthly views."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/sales/metrics"
        description="Sales dashboard metrics and charts (daily/weekly/monthly) will render here once the backend exposes sales endpoints. No placeholder figures will be presented as production data."
      />
    </div>
  );
}