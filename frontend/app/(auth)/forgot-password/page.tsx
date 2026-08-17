"use client";

import Link from "next/link";
import { RouteGuard } from "@/lib/permissions/route-guard";
import { Button } from "@/components/common";
import styles from "../auth.module.css";

export default function ForgotPasswordPage() {
  return (
    <RouteGuard requireAuth={false}>
      <div className={styles.layout}>
        <div className={styles.container}>
          <div className={styles.logo}>
            <div className={styles.logoIcon} aria-hidden="true">V</div>
            <span className={styles.logoText}>VitalEdge</span>
          </div>

          <div className={styles.card}>
            <div className={styles.heading}>
              <h1 className={styles.title}>Password reset</h1>
              <p className={styles.subtitle}>
                Password reset is not available yet. Please contact support and
                we&apos;ll help you regain access to your account.
              </p>
            </div>

            <Button type="button" fullWidth size="lg" variant="primary" disabled>
              Contact Support
            </Button>

            <p className={styles.footer}>
              <Link href="/login" className={styles.link}>
                Back to Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </RouteGuard>
  );
}