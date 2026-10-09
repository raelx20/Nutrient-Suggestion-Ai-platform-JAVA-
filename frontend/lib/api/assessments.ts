/* ================================================================
 * Assessments API — typed wrappers for /api/v1/assessments.
 * ================================================================ */

import { api } from "./client";
import type {
  AnswerBatchRequest,
  AnswerBatchResult,
  AssessmentDetailResponse,
  StartAssessmentResponse,
} from "@/types";

const BASE = "/api/v1/assessments";

export const assessmentsApi = {
  /** Start a new assessment — returns questions + assessment summary. */
  start(): Promise<StartAssessmentResponse> {
    return api.post(`${BASE}/start`);
  },

  /** Submit one or more answers to an assessment. */
  submitAnswers(
    assessmentId: string,
    data: AnswerBatchRequest
  ): Promise<AnswerBatchResult> {
    return api.post(`${BASE}/${assessmentId}/answers`, data);
  },

  /** Complete an assessment — triggers scoring and recommendations. */
  complete(assessmentId: string): Promise<AssessmentDetailResponse> {
    return api.post(`${BASE}/${assessmentId}/complete`);
  },

  /** Get assessment details. */
  get(assessmentId: string): Promise<AssessmentDetailResponse> {
    return api.get(`${BASE}/${assessmentId}`);
  },
};
