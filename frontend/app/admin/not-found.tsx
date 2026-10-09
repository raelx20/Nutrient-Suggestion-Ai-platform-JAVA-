import Link from "next/link";
import { Button, EmptyState } from "@/components/common";
import styles from "./error.module.css";

export default function AdminNotFound() {
  return (
    <div className={styles.wrap}>
      <EmptyState
        title="Page not found"
        description="The admin page you're looking for doesn't exist or has been moved."
        action={
          <Link href="/admin/dashboard">
            <Button variant="adminPrimary">Back to dashboard</Button>
          </Link>
        }
      />
    </div>
  );
}