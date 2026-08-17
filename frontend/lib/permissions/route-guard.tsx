"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

/* ================================================================
 * Route Guard — client-side route protection.
 *
 * NOT the security boundary. Backend authorization is authoritative.
 * This merely improves UX by redirecting unauthorized users.
 * ================================================================ */

export interface RouteGuardProps {
  /** Require authentication. Default: true. */
  requireAuth?: boolean;
  /** Require admin role. Default: false. */
  requireAdmin?: boolean;
  /** Redirect unauthenticated users here. Default: /login. */
  loginRedirect?: string;
  /** Redirect authenticated users away (for login/signup pages). */
  redirectIfAuthenticated?: string;
  /** Loading placeholder while checking auth. */
  loading?: React.ReactNode;
  children: React.ReactNode;
}

export function RouteGuard({
  requireAuth = true,
  requireAdmin = false,
  loginRedirect = "/login",
  redirectIfAuthenticated,
  loading,
  children,
}: RouteGuardProps) {
  const { isAuthenticated, isAdmin, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;

    /* Redirect authenticated users away from auth pages. */
    if (redirectIfAuthenticated && isAuthenticated) {
      router.replace(redirectIfAuthenticated);
      return;
    }

    /* Redirect unauthenticated users to login. */
    if (requireAuth && !isAuthenticated) {
      router.replace(loginRedirect);
      return;
    }

    /* Redirect non-admin users from admin routes. */
    if (requireAdmin && !isAdmin) {
      router.replace("/chat");
      return;
    }
  }, [
    isLoading,
    isAuthenticated,
    isAdmin,
    requireAuth,
    requireAdmin,
    loginRedirect,
    redirectIfAuthenticated,
    router,
  ]);

  /* Show loading state while checking. */
  if (isLoading) {
    return <>{loading ?? <LoadingScreen />}</>;
  }

  /* Suppress flash of protected content during redirect. */
  if (redirectIfAuthenticated && isAuthenticated) return null;
  if (requireAuth && !isAuthenticated) return null;
  if (requireAdmin && !isAdmin) return null;

  return <>{children}</>;
}

/* ---- Default loading screen ---- */

function LoadingScreen() {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        minHeight: "100dvh",
        background: "var(--white)",
      }}
    >
      <div
        style={{
          width: 32,
          height: 32,
          border: "3px solid var(--gray-200)",
          borderTop: "3px solid var(--consumer-primary-500)",
          borderRadius: "50%",
          animation: "spin 600ms linear infinite",
        }}
        role="status"
        aria-label="Loading"
      />
    </div>
  );
}
