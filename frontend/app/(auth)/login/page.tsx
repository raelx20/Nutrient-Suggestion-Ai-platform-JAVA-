"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { RouteGuard } from "@/lib/permissions/route-guard";
import { validateLogin } from "@/lib/validation";
import { Button, Input } from "@/components/common";
import styles from "../auth.module.css";

export default function LoginPage() {
  return (
    <RouteGuard
      requireAuth={false}
      redirectIfAuthenticated="/chat"
    >
      <LoginContent />
    </RouteGuard>
  );
}

function LoginContent() {
  const { login, error: authError, clearError } = useAuth();
  const router = useRouter();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    clearError();
    setFieldErrors({});

    const errors = validateLogin({ email, password });
    if (errors.length > 0) {
      const map: Record<string, string> = {};
      errors.forEach((err) => (map[err.field] = err.message));
      setFieldErrors(map);
      return;
    }

    setLoading(true);
    try {
      await login({ email, password });
      router.push("/chat");
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
          <div className={styles.heading}>
            <h1 className={styles.title}>Welcome back</h1>
            <p className={styles.subtitle}>Sign in to your account</p>
          </div>

          {authError && (
            <div className={`${styles.alert} ${styles.alertError}`} role="alert">
              {authError}
            </div>
          )}

          <form className={styles.form} onSubmit={handleSubmit} noValidate>
            <Input
              id="login-email"
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
              id="login-password"
              label="Password"
              type="password"
              placeholder="Enter your password"
              autoComplete="current-password"
              required
              showPasswordToggle
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={fieldErrors.password}
            />

            <div className={styles.forgotPassword}>
              <Link href="/forgot-password" className={styles.forgotPasswordLink}>
                Forgot password?
              </Link>
            </div>

            <Button
              type="submit"
              fullWidth
              size="lg"
              loading={loading}
              disabled={loading}
            >
              Sign In
            </Button>
          </form>

          <p className={styles.footer}>
            Don&apos;t have an account?{" "}
            <Link href="/signup" className={styles.link}>
              Create account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
