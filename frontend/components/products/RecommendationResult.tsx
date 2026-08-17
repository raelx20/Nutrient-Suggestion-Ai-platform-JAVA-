"use client";

import { useState } from "react";
import type { AssessmentDetailResponse, RecommendationResponse } from "@/types";
import { RecommendationCard } from "./RecommendationCard";
import { AlternativesList } from "./AlternativesList";
import { CounsellorCard } from "./CounsellorCard";
import styles from "./RecommendationResult.module.css";

export interface RecommendationResultProps {
  data: AssessmentDetailResponse;
  onViewProduct?: (productId: string) => void;
  onContactCounsellor?: () => void;
}

/**
 * Renders the backend's recommendation result set:
 * counsellor guidance (when flagged), the primary recommendation,
 * and (on demand) alternatives. Only consumer-safe fields are shown.
 */
export function RecommendationResult({
  data,
  onViewProduct,
  onContactCounsellor,
}: RecommendationResultProps) {
  const { healthProfile, recommendations } = data;
  const [showAlternatives, setShowAlternatives] = useState(false);

  const ranked = [...recommendations].sort((a, b) => a.rank - b.rank);
  const primary =
    ranked.find((r) => !r.excluded && r.safe) ??
    ranked.find((r) => !r.excluded);
  const alternatives: RecommendationResponse[] = ranked.filter(
    (r) => primary && r.id !== primary.id && !r.excluded
  );

  return (
    <div className={styles.stack}>
      {healthProfile?.counsellorRecommended && (
        <CounsellorCard onContact={onContactCounsellor} />
      )}

      {primary && (
        <RecommendationCard
          recommendation={primary}
          onViewProduct={onViewProduct}
          onShowAlternatives={
            alternatives.length > 0
              ? () => setShowAlternatives(true)
              : undefined
          }
        />
      )}

      {showAlternatives && (
        <AlternativesList
          alternatives={alternatives}
          onSelect={onViewProduct}
          onContactCounsellor={onContactCounsellor}
        />
      )}
    </div>
  );
}