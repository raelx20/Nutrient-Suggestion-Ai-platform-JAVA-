"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import styles from "./ProfileMenu.module.css";

/**
 * Consumer profile menu — intentionally minimal.
 * No admin features are exposed here.
 */
export function ProfileMenu() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  const initial = (user?.fullName ?? user?.email ?? "U").charAt(0).toUpperCase();

  const close = useCallback(() => setOpen(false), []);

  useEffect(() => {
    if (!open) return;
    const onPointerDown = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        close();
      }
    };
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") close();
    };
    document.addEventListener("mousedown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("mousedown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [open, close]);

  const handleLogout = async () => {
    setLoggingOut(true);
    await logout();
    router.push("/login");
  };

  return (
    <div className={styles.wrapper} ref={menuRef}>
      <button
        type="button"
        className={styles.avatarButton}
        onClick={() => setOpen((v) => !v)}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-label="Account menu"
      >
        <span className={styles.avatar} aria-hidden="true">
          {initial}
        </span>
      </button>

      {open && (
        <div className={styles.menu} role="menu">
          <div className={styles.menuHeader}>
            <p className={styles.name}>{user?.fullName || "VitalEdge user"}</p>
            <p className={styles.email}>{user?.email}</p>
          </div>
          <div className={styles.divider} />
          <button
            type="button"
            className={styles.menuItem}
            role="menuitem"
            disabled={loggingOut}
            onClick={handleLogout}
          >
            <span className={styles.menuItemLabel}>
              {loggingOut ? "Signing out…" : "Sign out"}
            </span>
          </button>
        </div>
      )}
    </div>
  );
}