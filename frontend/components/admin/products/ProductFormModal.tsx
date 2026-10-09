"use client";

import { useState, type FormEvent } from "react";
import type { ProductRequest, ProductResponse } from "@/types";
import { Modal, Button, Input, Select } from "@/components/common";
import styles from "./ProductFormModal.module.css";

const CATEGORY_OPTIONS = [
  { value: "low", label: "Low" },
  { value: "medium", label: "Medium" },
  { value: "high", label: "High" },
];

const AGE_GROUP_OPTIONS = [
  { value: "children", label: "Children" },
  { value: "adults", label: "Adults" },
  { value: "seniors", label: "Seniors" },
  { value: "all", label: "All" },
];

const STATUS_OPTIONS = [
  { value: "DRAFT", label: "Draft" },
  { value: "ACTIVE", label: "Active" },
  { value: "INACTIVE", label: "Inactive" },
  { value: "ARCHIVED", label: "Archived" },
];

export interface ProductFormModalProps {
  open: boolean;
  /** Existing product when editing. */
  product?: ProductResponse | null;
  loading?: boolean;
  error?: string | null;
  onClose: () => void;
  onSubmit: (data: ProductRequest) => void;
}

const EMPTY: ProductRequest = {
  name: "",
  sku: "",
  categoryCode: "",
  category: "medium",
  ageGroup: "adults",
  status: "DRAFT",
  description: "",
  allergens: [],
  dietaryTags: [],
  nutritionalInfo: {},
};

export function ProductFormModal({
  open,
  product,
  loading = false,
  error,
  onClose,
  onSubmit,
}: ProductFormModalProps) {
  const [form, setForm] = useState<ProductRequest>(product ? toRequest(product) : EMPTY);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  /* Reset the form when the modal opens for a new/edited product. */
  const [lastKey, setLastKey] = useState<string | null>(null);
  const key = product?.id ?? "new";
  if (open && lastKey !== key) {
    setLastKey(key);
    setForm(product ? toRequest(product) : EMPTY);
    setFieldErrors({});
  }

  const set = <K extends keyof ProductRequest>(field: K, value: ProductRequest[K]) => {
    setFieldErrors((prev) => {
      if (!(field in prev)) return prev;
      const next = { ...prev };
      delete next[field as string];
      return next;
    });
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    const errors: Record<string, string> = {};
    if (!form.name.trim()) errors.name = "Name is required";
    if (!form.sku.trim()) errors.sku = "SKU is required";
    if (!form.categoryCode.trim()) errors.categoryCode = "Category code is required";
    if (!form.category) errors.category = "Category is required";
    if (!form.ageGroup) errors.ageGroup = "Age group is required";
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    onSubmit({
      ...form,
      name: form.name.trim(),
      sku: form.sku.trim(),
      categoryCode: form.categoryCode.trim(),
    });
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={product ? "Edit product" : "New product"}
      footer={
        <>
          <Button type="button" variant="ghost" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            type="submit"
            form="product-form"
            variant="adminPrimary"
            loading={loading}
            disabled={loading}
          >
            {product ? "Save changes" : "Create product"}
          </Button>
        </>
      }
    >
      <form id="product-form" className={styles.form} onSubmit={handleSubmit} noValidate>
        {error && (
          <p className={styles.error} role="alert">
            {error}
          </p>
        )}

        <div className={styles.grid2}>
          <Input
            id="product-name"
            label="Name"
            placeholder="Whey Protein Isolate"
            required
            value={form.name}
            onChange={(e) => set("name", e.target.value)}
            error={fieldErrors.name}
          />
          <Input
            id="product-sku"
            label="SKU"
            placeholder="SKU-0001"
            required
            value={form.sku}
            onChange={(e) => set("sku", e.target.value)}
            error={fieldErrors.sku}
          />
        </div>

        <div className={styles.grid2}>
          <Input
            id="product-category-code"
            label="Category code"
            placeholder="SPORTS"
            required
            value={form.categoryCode}
            onChange={(e) => set("categoryCode", e.target.value)}
            error={fieldErrors.categoryCode}
          />
          <Select
            id="product-category"
            label="Category"
            options={CATEGORY_OPTIONS}
            value={form.category}
            required
            onChange={(e) => set("category", e.target.value)}
            error={fieldErrors.category}
          />
        </div>

        <div className={styles.grid2}>
          <Select
            id="product-age-group"
            label="Age group"
            options={AGE_GROUP_OPTIONS}
            value={form.ageGroup}
            required
            onChange={(e) => set("ageGroup", e.target.value)}
            error={fieldErrors.ageGroup}
          />
          <Select
            id="product-status"
            label="Status"
            options={STATUS_OPTIONS}
            value={form.status ?? "DRAFT"}
            onChange={(e) => set("status", e.target.value)}
          />
        </div>

        <Input
          id="product-description"
          label="Description"
          placeholder="Short description of the product."
          value={form.description ?? ""}
          onChange={(e) => set("description", e.target.value)}
        />

        <div className={styles.grid2}>
          <Input
            id="product-allergens"
            label="Allergens"
            placeholder="dairy, nuts"
            value={(form.allergens ?? []).join(", ")}
            onChange={(e) =>
              set(
                "allergens",
                e.target.value.split(",").map((s) => s.trim()).filter(Boolean)
              )
            }
          />
          <Input
            id="product-dietary-tags"
            label="Dietary tags"
            placeholder="high_protein, balanced"
            value={(form.dietaryTags ?? []).join(", ")}
            onChange={(e) =>
              set(
                "dietaryTags",
                e.target.value.split(",").map((s) => s.trim()).filter(Boolean)
              )
            }
          />
        </div>

        <Input
          id="product-nutritional-info"
          label="Nutritional info (JSON)"
          placeholder='{"protein_per_100g": 85}'
          value={safeJson(form.nutritionalInfo)}
          onChange={(e) => {
            try {
              const parsed = e.target.value.trim() ? JSON.parse(e.target.value) : {};
              set("nutritionalInfo", parsed);
            } catch {
              /* Invalid JSON — keep the previous value. */
            }
          }}
        />
      </form>
    </Modal>
  );
}

function toRequest(p: ProductResponse): ProductRequest {
  return {
    name: p.name,
    sku: p.sku,
    categoryCode: p.categoryCode,
    category: p.category ?? "medium",
    ageGroup: p.ageGroup ?? "adults",
    status: p.status ?? "DRAFT",
    description: p.description ?? "",
    allergens: p.allergens ?? [],
    dietaryTags: p.dietaryTags ?? [],
    nutritionalInfo: p.nutritionalInfo ?? {},
  };
}

function safeJson(value: Record<string, unknown> | null | undefined): string {
  try {
    return value && Object.keys(value).length > 0 ? JSON.stringify(value) : "";
  } catch {
    return "";
  }
}