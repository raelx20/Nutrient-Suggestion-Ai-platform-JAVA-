"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useAuth } from "@/lib/auth";
import { RouteGuard } from "@/lib/permissions/route-guard";
import { validateSignup } from "@/lib/validation";
import { Button, Input } from "@/components/common";
import styles from "../auth.module.css";

export default function SignupPage() {
  return (
    <RouteGuard
      requireAuth={false}
      redirectIfAuthenticated="/chat"
    >
      <SignupContent />
    </RouteGuard>
  );
}

function SignupContent() {
  const { signup, error: authError, clearError } = useAuth();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    clearError();
    setFieldErrors({});

    const errors = validateSignup({ fullName, email, password, confirmPassword });
    if (errors.length > 0) {
      const map: Record<string, string> = {};
      errors.forEach((err) => (map[err.field] = err.message));
      setFieldErrors(map);
      return;
    }

    setLoading(true);
    try {
      await signup({ email, password, fullName: fullName.trim() || undefined });
      setSuccess(true);
    } catch {
      /* Error is set in auth context */
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.layout}>
      <div className={styles.container}>
        {/* Logo */}
        <div className={styles.logo}>
          <div className={styles.logoIcon} aria-hidden="true">V</div>
          <span className={styles.logoText}>VitalEdge</span>
        </div>

        {/* Card */}
        <div className={styles.card}>
          {success ? (
            /* Success state */
            <div className={styles.heading}>
              <h1 className={styles.title}>Check your email</h1>
              <p className={styles.subtitle}>
                We&apos;ve sent a verification link to <strong>{email}</strong>.
                Please verify your email address to sign in.
              </p>
              <p className={styles.footer} style={{ marginTop: "var(--space-6)" }}>
                <Link href="/login" className={styles.link}>
                  Go to Sign In
                </Link>
              </p>
            </div>
          ) : (
            <>
              <div className={styles.heading}>
                <h1 className={styles.title}>Create your account</h1>
                <p className={styles.subtitle}>Start your nutrition journey</p>
              </div>

              {authError && (
                <div className={`${styles.alert} ${styles.alertError}`} role="alert">
                  {authError}
                </div>
              )}

              <form className={styles.form} onSubmit={handleSubmit} noValidate>
                <Input
                  id="signup-fullname"
                  label="Full Name"
                  type="text"
                  placeholder="Your full name"
                  autoComplete="name"
                  required
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  error={fieldErrors.fullName}
                />

                <Input
                  id="signup-email"
                  label="Email"
                  type="email"
                  placeholder="you@example.com"
                  autoComplete="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  error={fieldErrors.email}
                />

                <Input
                  id="signup-password"
                  label="Password"
                  type="password"
                  placeholder="At least 8 characters"
                  autoComplete="new-password"
                  required
                  showPasswordToggle
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  error={fieldErrors.password}
                  hint="Minimum 8 characters"
                />

                <Input
                  id="signup-confirm-password"
                  label="Confirm Password"
                  type="password"
                  placeholder="Re-enter your password"
                  autoComplete="new-password"
                  required
                  showPasswordToggle
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  error={fieldErrors.confirmPassword}
                />

                <Button
                  type="submit"
                  fullWidth
                  size="lg"
                  loading={loading}
                  disabled={loading}
                >
                  Create Account
                </Button>
              </form>

              <p className={styles.footer}>
                Already have an account?{" "}
                <Link href="/login" className={styles.link}>
                  Sign in
                </Link>
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
