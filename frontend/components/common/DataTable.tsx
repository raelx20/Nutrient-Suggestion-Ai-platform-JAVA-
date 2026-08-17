"use client";

import { cn } from "@/lib/utils";
import styles from "./DataTable.module.css";

export type SortDirection = "asc" | "desc";

export interface DataTableColumn<T> {
  key: string;
  header: string;
  sortable?: boolean;
  /** Custom cell renderer. Defaults to (row as any)[key]. */
  render?: (row: T) => React.ReactNode;
  /** Width hint for the column. */
  width?: string;
}

export interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  rows: T[];
  rowKey: (row: T) => string;
  sortKey?: string;
  sortDirection?: SortDirection;
  onSort?: (key: string) => void;
  emptyMessage?: string;
  emptyAction?: React.ReactNode;
  isLoading?: boolean;
}

export function DataTable<T>({
  columns,
  rows,
  rowKey,
  sortKey,
  sortDirection = "asc",
  onSort,
  emptyMessage = "No records found.",
  emptyAction,
  isLoading = false,
}: DataTableProps<T>) {
  if (isLoading) {
    return <div className={styles.loading} role="status">Loading…</div>;
  }

  if (rows.length === 0) {
    return (
      <div className={styles.empty}>
        <p className={styles.emptyMessage}>{emptyMessage}</p>
        {emptyAction && <div className={styles.emptyAction}>{emptyAction}</div>}
      </div>
    );
  }

  return (
    <div className={styles.tableWrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                scope="col"
                style={col.width ? { width: col.width } : undefined}
                aria-sort={
                  sortKey === col.key
                    ? sortDirection === "asc"
                      ? "ascending"
                      : "descending"
                    : undefined
                }
              >
                {col.sortable && onSort ? (
                  <button
                    type="button"
                    className={styles.sortButton}
                    onClick={() => onSort(col.key)}
                  >
                    {col.header}
                    {sortKey === col.key && (
                      <span className={styles.sortIcon} aria-hidden="true">
                        {sortDirection === "asc" ? "▲" : "▼"}
                      </span>
                    )}
                  </button>
                ) : (
                  col.header
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={rowKey(row)}>
              {columns.map((col) => (
                <td key={col.key} className={cn(styles.cell, col.width && styles.truncate)}>
                  {col.render
                    ? col.render(row)
                    : ((row as Record<string, unknown>)[col.key] as React.ReactNode) ?? "—"}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}