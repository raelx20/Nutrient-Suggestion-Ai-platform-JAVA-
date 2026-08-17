"use client";

import { useState, useEffect } from "react";
import { adminApi, ApiError } from "@/lib/api";
import type { AdminDashboardMetrics } from "@/lib/api/admin";
import { MetricCard, DashboardGrid } from "@/components/admin/dashboard";
import { LoadingSkeleton, EmptyState, Card } from "@/components/common";
import { AwaitingBackend } from "@/components/admin";
import styles from "./page.module.css";

export default function AdminDashboardPage() {
  const [metrics, setMetrics] = useState<AdminDashboardMetrics | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [missing, setMissing] = useState(false);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setIsLoading(true);
      try {
        const data = await adminApi.getDashboardMetrics();
        if (!cancelled) setMetrics(data);
      } catch (err) {
        if (!cancelled && err instanceof ApiError && err.status === 404) {
          setMissing(true);
        }
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, []);

  if (isLoading) {
    return (
      <div className={styles.page}>
        <div className={styles.gridSkeleton}>
          <LoadingSkeleton variant="card" height={120} />
          <LoadingSkeleton variant="card" height={120} />
          <LoadingSkeleton variant="card" height={120} />
          <LoadingSkeleton variant="card" height={120} />
        </div>
      </div>
    );
  }

  if (missing || !metrics) {
    return (
      <div className={styles.page}>
        <AwaitingBackend
          endpoint="GET /api/v1/admin/dashboard/metrics"
          description="The dashboard will show live metrics (users, assessments, recommendations, sales) once this endpoint is implemented in the backend."
        />
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <DashboardGrid>
        <MetricCard label="Total Users" value={formatNum(metrics.totalUsers)} trend="Live" />
        <MetricCard label="Active Users" value={formatNum(metrics.activeUsers)} />
        <MetricCard label="Completed Assessments" value={formatNum(metrics.completedAssessments)} />
        <MetricCard label="Recommendations" value={formatNum(metrics.totalRecommendations)} />
        <MetricCard label="Product Selections" value={formatNum(metrics.productSelections)} />
        <MetricCard label="Counsellor Referrals" value={formatNum(metrics.counsellorReferrals)} />
        <MetricCard label="Sales" value={formatCurrency(metrics.totalSales)} />
        <MetricCard label="Conversion Rate" value={formatPct(metrics.conversionRate)} />
      </DashboardGrid>

      <div className={styles.activityGrid}>
        <Card padding="none">
          <ActivitySection title="Recent Users" />
        </Card>
        <Card padding="none">
          <ActivitySection title="Recent Assessments" />
        </Card>
        <Card padding="none">
          <ActivitySection title="Recent Recommendations" />
        </Card>
        <Card padding="none">
          <ActivitySection title="Recent Product Selections" />
        </Card>
      </div>
    </div>
  );
}

function ActivitySection({ title }: { title: string }) {
  return (
    <div className={styles.activity}>
      <h2 className={styles.activityTitle}>{title}</h2>
      <EmptyState
        title="No data yet"
        description="Awaiting the corresponding backend list endpoint."
      />
    </div>
  );
}

function formatNum(value: number): string {
  return value.toLocaleString();
}

function formatCurrency(value: number): string {
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  }).format(value);
}

function formatPct(value: number): string {
  return `${value.toFixed(1)}%`;
}