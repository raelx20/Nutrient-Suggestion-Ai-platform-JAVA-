/* ================================================================
 * Admin navigation configuration.
 * ================================================================ */

export interface SidebarNavItem {
  label: string;
  href: string;
  /** Optional small SVG path(s) rendered inside a 18px icon. */
  icon?: "dashboard" | "users" | "assessments" | "conversations" | "recommendations" | "products" | "questions" | "ai" | "sales" | "analytics" | "counsellors" | "audit" | "settings";
}

export interface SidebarNavGroup {
  label: string;
  items: SidebarNavItem[];
}

export const adminNavGroups: SidebarNavGroup[] = [
  {
    label: "Workspace",
    items: [
      { label: "Dashboard", href: "/admin/dashboard", icon: "dashboard" },
      { label: "Users", href: "/admin/users", icon: "users" },
      { label: "Assessments", href: "/admin/assessments", icon: "assessments" },
      { label: "Conversations", href: "/admin/conversations", icon: "conversations" },
      { label: "Recommendations", href: "/admin/recommendations", icon: "recommendations" },
    ],
  },
  {
    label: "Catalog",
    items: [
      { label: "Products", href: "/admin/products", icon: "products" },
      { label: "Questions", href: "/admin/questions", icon: "questions" },
    ],
  },
  {
    label: "AI",
    items: [{ label: "AI Control Center", href: "/admin/ai", icon: "ai" }],
  },
  {
    label: "Business",
    items: [
      { label: "Sales", href: "/admin/sales", icon: "sales" },
      { label: "Counsellors", href: "/admin/counsellors", icon: "counsellors" },
    ],
  },
  {
    label: "Insights",
    items: [{ label: "Analytics", href: "/admin/analytics", icon: "analytics" }],
  },
  {
    label: "Security",
    items: [{ label: "Audit Logs", href: "/admin/audit", icon: "audit" }],
  },
  {
    label: "General",
    items: [{ label: "Settings", href: "/admin/settings", icon: "settings" }],
  },
];

export const adminNavItems = adminNavGroups.flatMap((g) => g.items);