"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { AdminSearch } from "./AdminSearch";
import styles from "./AdminHeader.module.css";

export interface AdminHeaderProps {
  /** Page title shown on smaller screens. */
  title?: string;
  /** Toggle the mobile sidebar drawer. */
  onMenuClick: () => void;
}

export function AdminHeader({ title, onMenuClick }: AdminHeaderProps) {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [profileOpen, setProfileOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);

  const initial = (user?.fullName ?? user?.email ?? "A")
    .charAt(0)
    .toUpperCase();

  const closeProfile = useCallback(() => setProfileOpen(false), []);

  useEffect(() => {
    if (!profileOpen) return;
    const onPointerDown = (e: MouseEvent) => {
      if (profileRef.current && !profileRef.current.contains(e.target as Node)) {
        closeProfile();
      }
    };
    document.addEventListener("mousedown", onPointerDown);
    return () => document.removeEventListener("mousedown", onPointerDown);
  }, [profileOpen, closeProfile]);

  const handleLogout = async () => {
    setLoggingOut(true);
    await logout();
    router.push("/login");
  };

  return (
    <header className={styles.header}>
      <div className={styles.left}>
        <button
          type="button"
          className={styles.menuButton}
          onClick={onMenuClick}
          aria-label="Toggle navigation"
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <line x1="3" y1="6" x2="21" y2="6" />
            <line x1="3" y1="12" x2="21" y2="12" />
            <line x1="3" y1="18" x2="21" y2="18" />
          </svg>
        </button>
        {title && <h1 className={styles.title}>{title}</h1>}
      </div>

      <div className={styles.right}>
        <div className={styles.searchHide}>
          <AdminSearch />
        </div>

        <button
          type="button"
          className={styles.iconButton}
          aria-label="Notifications"
        >
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
        </button>

        <div className={styles.profile} ref={profileRef}>
          <button
            type="button"
            className={styles.avatarButton}
            onClick={() => setProfileOpen((v) => !v)}
            aria-haspopup="menu"
            aria-expanded={profileOpen}
            aria-label="Admin profile menu"
          >
            <span className={styles.avatar} aria-hidden="true">
              {initial}
            </span>
            <span className={styles.profileName}>{user?.fullName || "Admin"}</span>
          </button>

          {profileOpen && (
            <div className={styles.menu} role="menu">
              <div className={styles.menuHeader}>
                <p className={styles.menuName}>{user?.fullName || "Admin user"}</p>
                <p className={styles.menuEmail}>{user?.email}</p>
              </div>
              <div className={styles.divider} />
              <button
                type="button"
                className={styles.menuItem}
                role="menuitem"
                disabled={loggingOut}
                onClick={handleLogout}
              >
                {loggingOut ? "Signing out…" : "Sign out"}
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}