"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { productsApi, ApiError } from "@/lib/api";
import { ProductCard } from "@/components/products";
import { EmptyState, LoadingSkeleton } from "@/components/common";
import type { ProductResponse } from "@/types";
import styles from "./page.module.css";

export default function ProductDetailPage() {
  const params = useParams<{ id: string }>();
  const id = params?.id ?? "";

  const [product, setProduct] = useState<ProductResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setIsLoading(true);
      setError(null);
      try {
        const data = await productsApi.detail(id);
        if (!cancelled) setProduct(data);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof ApiError && err.status === 404
              ? "This product is no longer available."
              : "Unable to load this product. Please try again."
          );
        }
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [id]);

  return (
    <div className={styles.page}>
      <Link href="/chat" className={styles.back}>
        ← Back to chat
      </Link>

      {isLoading && (
        <div className={styles.skeleton}>
          <LoadingSkeleton variant="card" width={420} height={320} />
        </div>
      )}

      {error && (
        <EmptyState
          title="Product unavailable"
          description={error}
          action={
            <Link href="/chat" className={styles.link}>
              Return to chat
            </Link>
          }
        />
      )}

      {product && !error && (
        <div className={styles.product}>
          <ProductCard product={product} />
        </div>
      )}
    </div>
  );
}