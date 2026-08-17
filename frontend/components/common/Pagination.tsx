"use client";

import styles from "./Pagination.module.css";

export interface PaginationProps {
  page: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  onPageChange: (page: number) => void;
}

export function Pagination({
  page,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
}: PaginationProps) {
  if (totalPages <= 1) {
    return (
      <div className={styles.pagination}>
        <span className={styles.info}>{totalElements} items</span>
      </div>
    );
  }

  const from = page * pageSize + 1;
  const to = Math.min((page + 1) * pageSize, totalElements);

  return (
    <nav className={styles.pagination} aria-label="Pagination">
      <span className={styles.info}>
        {from}–{to} of {totalElements}
      </span>
      <div className={styles.controls}>
        <button
          type="button"
          className={styles.button}
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
          aria-label="Previous page"
        >
          ←
        </button>
        <span className={styles.pageInfo}>
          Page {page + 1} of {totalPages}
        </span>
        <button
          type="button"
          className={styles.button}
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
          aria-label="Next page"
        >
          →
        </button>
      </div>
    </nav>
  );
}