"use client";

import { AuthProvider } from "@/lib/auth";

/**
 * Client-side providers wrapper.
 * Separated from root layout so the layout itself stays a Server Component.
 */
export function Providers({ children }: { children: React.ReactNode }) {
  return <AuthProvider>{children}</AuthProvider>;
}
