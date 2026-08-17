"use client";

import { useEffect } from "react";
import { Button, EmptyState } from "@/components/common";
import styles from "./error.module.css";

export interface AdminErrorProps {
  error: Error & { digest?: string };
  reset: () => void;
}

export default function AdminError({ error, reset }: AdminErrorProps) {
  useEffect(() => {
    console.error("Admin page error:", error);
  }, [error]);

  return (
    <div className={styles.wrap}>
      <EmptyState
        title="Something went wrong"
        description="An unexpected error occurred while loading this page. Please try again."
        action={
          <Button variant="adminPrimary" onClick={reset}>
            Try again
          </Button>
        }
      />
    </div>
  );
}