"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { RouteGuard } from "@/lib/permissions/route-guard";
import { authApi } from "@/lib/api";
import { Button } from "@/components/common";
import styles from "../auth.module.css";

export default function VerifyPage() {
  return (
    <RouteGuard requireAuth={false}>
      <Suspense fallback={<VerificationLoading />}>
        <VerifyContent />
      </Suspense>
    </RouteGuard>
  );
}

function VerificationLoading() {
  return (
    <div className={styles.layout}>
      <div className={styles.container}>
        <div className={styles.logo}>
          <div className={styles.logoIcon} aria-hidden="true">V</div>
          <span className={styles.logoText}>VitalEdge</span>
        </div>
        <div className={styles.card}>
          <div className={styles.heading}>
            <h1 className={styles.title}>Verifying your email</h1>
          </div>
        </div>
      </div>
    </div>
  );
}

type VerifyState = "loading" | "success" | "error";

const NO_TOKEN_MESSAGE =
  "No verification token was provided. Please use the link from your email.";

function VerifyContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const token = searchParams.get("token");

  const [state, setState] = useState<VerifyState>(token ? "loading" : "error");
  const [message, setMessage] = useState(token ? "" : NO_TOKEN_MESSAGE);

  useEffect(() => {
    if (!token) return;

    let cancelled = false;
    authApi
      .verifyEmail({ token })
      .then((res) => {
        if (cancelled) return;
        setState("success");
        setMessage(res.message);
      })
      .catch((err: unknown) => {
        if (cancelled) return;
        setState("error");
        setMessage(
          err instanceof Error
            ? err.message
            : "We couldn't verify your email. The link may be invalid or expired."
        );
      });

    return () => {
      cancelled = true;
    };
  }, [token]);

  return (
    <div className={styles.layout}>
      <div className={styles.container}>
        <div className={styles.logo}>
          <div className={styles.logoIcon} aria-hidden="true">V</div>
          <span className={styles.logoText}>VitalEdge</span>
        </div>

        <div className={styles.card}>
          {state === "loading" && (
            <div className={styles.heading}>
              <h1 className={styles.title}>Verifying your email</h1>
              <p className={styles.subtitle}>One moment…</p>
            </div>
          )}

          {state === "success" && (
            <>
              <div className={styles.heading}>
                <h1 className={styles.title}>Email verified</h1>
                <div className={`${styles.alert} ${styles.alertSuccess}`} role="status">
                  {message || "Your email has been verified successfully."}
                </div>
              </div>
              <Button
                type="button"
                fullWidth
                size="lg"
                variant="primary"
                onClick={() => router.push("/login")}
              >
                Sign in
              </Button>
            </>
          )}

          {state === "error" && (
            <>
              <div className={styles.heading}>
                <h1 className={styles.title}>Verification failed</h1>
                <div className={`${styles.alert} ${styles.alertError}`} role="alert">
                  {message}
                </div>
              </div>
              <div className={styles.footer}>
                <Link href="/login" className={styles.link}>
                  Back to Sign In
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}