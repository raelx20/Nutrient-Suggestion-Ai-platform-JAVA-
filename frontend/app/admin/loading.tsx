import { LoadingSkeleton } from "@/components/common";
import styles from "./loading.module.css";

export default function AdminLoading() {
  return (
    <div className={styles.wrap}>
      <LoadingSkeleton variant="heading" width={220} />
      <LoadingSkeleton variant="text" width={360} />
      <div className={styles.card}>
        <LoadingSkeleton variant="text" height={24} />
        <LoadingSkeleton variant="text" height={24} />
        <LoadingSkeleton variant="text" height={24} />
        <LoadingSkeleton variant="text" height={24} />
      </div>
    </div>
  );
}