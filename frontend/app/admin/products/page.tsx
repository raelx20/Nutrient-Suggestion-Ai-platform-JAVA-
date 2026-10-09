"use client";

import { useState, useEffect, useCallback } from "react";
import { productsApi, ApiError } from "@/lib/api";
import type { ProductRequest, ProductResponse } from "@/types";
import { AdminPageHeader } from "@/components/admin";
import { ProductFormModal } from "@/components/admin/products";
import {
  Button,
  Card,
  DataTable,
  EmptyState,
  FilterPanel,
  LoadingSkeleton,
  Modal,
  Pagination,
  SearchInput,
  Select,
  Badge,
} from "@/components/common";
import type { DataTableColumn } from "@/components/common";
import { humanize, formatDate } from "@/lib/utils";
import styles from "../admin-pages.module.css";

const STATUS_VARIANT: Record<string, "success" | "warning" | "neutral" | "error" | "info"> = {
  ACTIVE: "success",
  DRAFT: "warning",
  INACTIVE: "neutral",
  ARCHIVED: "error",
};

export default function AdminProductsPage() {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [q, setQ] = useState("");
  const [category, setCategory] = useState("");
  const [ageGroup, setAgeGroup] = useState("");
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const pageSize = 20;

  /* ---- Modal state ---- */
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<ProductResponse | null>(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ProductResponse | null>(null);
  const [deleting, setDeleting] = useState(false);

  /* Fetches without touching loading state (safe for effect use). */
  const fetchList = useCallback(async () => {
    const data = await productsApi.list({
      q: q || undefined,
      category: category || undefined,
      age_group: ageGroup || undefined,
      page,
      size: pageSize,
    });
    setProducts(data);
    /* The current backend returns a flat list; derive total from length. */
    setTotal(data.length);
  }, [q, category, ageGroup, page]);

  /* Event-handler refresh (shows the loading skeleton). */
  const refresh = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      await fetchList();
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Unable to load products. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  }, [fetchList]);

  useEffect(() => {
    let cancelled = false;
    productsApi
      .list({
        q: q || undefined,
        category: category || undefined,
        age_group: ageGroup || undefined,
        page,
        size: pageSize,
      })
      .then((data) => {
        if (cancelled) return;
        setProducts(data);
        /* The current backend returns a flat list; derive total from length. */
        setTotal(data.length);
        setError(null);
      })
      .catch((err: unknown) => {
        if (cancelled) return;
        setError(
          err instanceof ApiError ? err.message : "Unable to load products. Please try again."
        );
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [q, category, ageGroup, page]);

  const handleSearch = (value: string) => {
    setQ(value);
    setPage(0);
  };

  const openCreate = () => {
    setEditing(null);
    setFormError(null);
    setFormOpen(true);
  };

  const openEdit = (product: ProductResponse) => {
    setEditing(product);
    setFormError(null);
    setFormOpen(true);
  };

  const handleSubmit = async (data: ProductRequest) => {
    setSaving(true);
    setFormError(null);
    try {
      if (editing) {
        await productsApi.update(editing.id, data);
      } else {
        await productsApi.create(data);
      }
      setFormOpen(false);
      await refresh();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Could not save the product.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await productsApi.delete(deleteTarget.id);
      setDeleteTarget(null);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not delete the product.");
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  };

  const columns: DataTableColumn<ProductResponse>[] = [
    {
      key: "name",
      header: "Name",
      render: (p) => <span className={styles.nameCell}>{p.name}</span>,
    },
    { key: "sku", header: "SKU" },
    {
      key: "category",
      header: "Category",
      render: (p) => humanize(p.category ?? ""),
    },
    {
      key: "ageGroup",
      header: "Age Group",
      render: (p) => humanize(p.ageGroup ?? ""),
    },
    {
      key: "status",
      header: "Status",
      render: (p) => (
        <Badge variant={STATUS_VARIANT[p.status ?? ""] ?? "neutral"}>
          {humanize(p.status ?? "")}
        </Badge>
      ),
    },
    {
      key: "updatedAt",
      header: "Updated",
      render: (p) => formatDate(p.updatedAt),
    },
    {
      key: "actions",
      header: "Actions",
      render: (p) => (
        <div className={styles.rowActions}>
          <Button size="sm" variant="outline" onClick={() => openEdit(p)}>
            Edit
          </Button>
          <Button size="sm" variant="danger" onClick={() => setDeleteTarget(p)}>
            Delete
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="Products"
        description="Manage the product catalog. Create, edit, and deactivate products."
        actions={
          <Button variant="adminPrimary" onClick={openCreate}>
            New Product
          </Button>
        }
      />

      <Card padding="none">
        <div className={styles.cardHeader}>
          <SearchInput
            placeholder="Search products…"
            debounceMs={400}
            onChange={handleSearch}
          />
        </div>

        <div className={styles.cardBody}>
          <FilterPanel onReset={() => { setCategory(""); setAgeGroup(""); setPage(0); }}>
            <Select
              label="Category"
              options={[
                { value: "", label: "All categories" },
                { value: "low", label: "Low" },
                { value: "medium", label: "Medium" },
                { value: "high", label: "High" },
              ]}
              value={category}
              onChange={(e) => { setCategory(e.target.value); setPage(0); }}
            />
            <Select
              label="Age group"
              options={[
                { value: "", label: "All age groups" },
                { value: "children", label: "Children" },
                { value: "adults", label: "Adults" },
                { value: "seniors", label: "Seniors" },
                { value: "all", label: "All" },
              ]}
              value={ageGroup}
              onChange={(e) => { setAgeGroup(e.target.value); setPage(0); }}
            />
          </FilterPanel>
        </div>

        {error && (
          <div className={styles.inlineError} role="alert">
            {error}
          </div>
        )}

        {isLoading ? (
          <div className={styles.skeletonTable}>
            <LoadingSkeleton variant="text" height={24} />
            <LoadingSkeleton variant="text" height={24} />
            <LoadingSkeleton variant="text" height={24} />
            <LoadingSkeleton variant="text" height={24} />
          </div>
        ) : (
          <DataTable
            columns={columns}
            rows={products}
            rowKey={(p) => p.id}
            emptyMessage="No products match your filters."
            emptyAction={
              <Button variant="outline" size="sm" onClick={openCreate}>
                Create a product
              </Button>
            }
          />
        )}

        <Pagination
          page={page}
          totalPages={Math.max(1, Math.ceil(total / pageSize))}
          totalElements={total}
          pageSize={pageSize}
          onPageChange={setPage}
        />
      </Card>

      <EmptyState
        title="Note"
        description="Listing draft/inactive/archived products (all statuses) requires GET /api/v1/admin/products, which is awaiting backend implementation. This list currently shows active products via the public endpoint."
      />

      {/* Create / Edit modal */}
      <ProductFormModal
        open={formOpen}
        product={editing}
        loading={saving}
        error={formError}
        onClose={() => setFormOpen(false)}
        onSubmit={handleSubmit}
      />

      {/* Delete confirmation */}
      <Modal
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="Delete product"
        footer={
          <>
            <Button variant="ghost" onClick={() => setDeleteTarget(null)} disabled={deleting}>
              Cancel
            </Button>
            <Button variant="danger" onClick={handleDelete} loading={deleting} disabled={deleting}>
              Delete
            </Button>
          </>
        }
      >
        <p>
          Are you sure you want to delete{" "}
          <strong>{deleteTarget?.name}</strong>? This cannot be undone.
        </p>
      </Modal>
    </div>
  );
}