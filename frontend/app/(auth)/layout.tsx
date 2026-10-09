import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Sign In — VitalEdge",
  description: "Sign in to your VitalEdge nutrition assistant account.",
};

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <>{children}</>;
}
