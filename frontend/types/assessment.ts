/* ================================================================
 * Assessment DTOs — mirrors Spring Boot assessment records.
 * ================================================================ */

import type { AssessmentStatus } from "./enums";

/** Assessment summary with progress */
export interface AssessmentSummary {
  id: string;
  status: AssessmentStatus;
  questionnaireId: string | null;
  questionnaireCode: string | null;
  totalQuestions: number;
  answeredQuestions: number;
  progressPercent: number;
  startedAt: string;
  completedAt: string | null;
}

/** Single question in the questionnaire */
export interface QuestionResponse {
  id: string;
  key: string;
  text: string;
  type: string;
  options: string[] | null;
  required: boolean;
  orderIndex: number;
}

/** POST /api/v1/assessments/start response */
export interface StartAssessmentResponse {
  assessment: AssessmentSummary;
  questions: QuestionResponse[];
}

/** Single answer submission */
export interface AnswerRequest {
  questionId: string;
  value: unknown;
}

/** Batch answer submission body */
export interface AnswerBatchRequest {
  answers: AnswerRequest[];
}

/** Single answer response */
export interface AnswerResponse {
  questionId: string;
  questionKey: string;
  value: unknown;
}

/** Batch answer result */
export interface AnswerBatchResult {
  answers: AnswerResponse[];
  assessment: AssessmentSummary;
}

/** Health profile computed by backend after assessment completion */
export interface HealthProfileResponse {
  bmi: number;
  bmiClassification: string;
  scores: Record<string, unknown>;
  riskFactors: string[];
  counsellorRecommended: boolean;
  profileData: Record<string, unknown>;
  version: number;
}

/** Score component within a recommendation */
export interface ScoreComponent {
  metric: string;
  value: number;
}

/** Product recommendation from assessment */
export interface RecommendationResponse {
  id: string;
  productId: string;
  productName: string;
  productSku: string;
  productCategory: string;
  productCategoryCode: string;
  productAgeGroup: string;
  reason: string;
  confidence: number;
  rank: number;
  safe: boolean;
  excluded: boolean;
  scoreComponents: ScoreComponent[];
}

/** Full assessment detail (returned on complete or get) */
export interface AssessmentDetailResponse {
  id: string;
  status: AssessmentStatus;
  assessment: AssessmentSummary;
  healthProfile: HealthProfileResponse | null;
  recommendations: RecommendationResponse[];
}
