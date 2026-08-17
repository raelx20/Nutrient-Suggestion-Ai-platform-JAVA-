"use client";

import { useState, useEffect } from "react";
import { usePathname } from "next/navigation";
import { RouteGuard } from "@/lib/permissions/route-guard";
import { AdminSidebar, AdminHeader } from "@/components/admin";
import styles from "./admin.module.css";

function pageTitle(pathname: string): string {
  if (pathname === "/admin") return "Dashboard";
  const segments = pathname.split("/").filter(Boolean);
  const last = segments[segments.length - 1];
  return last
    .split("-")
    .map((s) => s.charAt(0).toUpperCase() + s.slice(1))
    .join(" ");
}

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  /* Close the mobile drawer on Escape and lock body scroll while open. */
  useEffect(() => {
    if (!sidebarOpen) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") setSidebarOpen(false);
    };
    document.addEventListener("keydown", onKeyDown);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = "";
    };
  }, [sidebarOpen]);

  return (
    <RouteGuard requireAuth requireAdmin>
      <div className={styles.shell}>
        {/* Desktop sidebar */}
        <div className={styles.desktopSidebar}>
          <AdminSidebar />
        </div>

        {/* Mobile drawer */}
        {sidebarOpen && (
          <>
            <div
              className={styles.overlay}
              onClick={() => setSidebarOpen(false)}
              aria-hidden="true"
            />
            <div className={styles.drawer} role="dialog" aria-label="Admin navigation">
              <AdminSidebar onClose={() => setSidebarOpen(false)} />
            </div>
          </>
        )}

        <div className={styles.content}>
          <AdminHeader
            title={pageTitle(pathname)}
            onMenuClick={() => setSidebarOpen(true)}
          />
          <main className={styles.main}>{children}</main>
        </div>
      </div>
    </RouteGuard>
  );
}