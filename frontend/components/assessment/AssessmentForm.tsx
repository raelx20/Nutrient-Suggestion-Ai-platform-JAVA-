"use client";

import { useState, useCallback } from "react";
import type {
  AnswerBatchResult,
  AssessmentDetailResponse,
  StartAssessmentResponse,
} from "@/types";
import { assessmentsApi, ApiError } from "@/lib/api";
import { Button } from "@/components/common";
import { AssessmentProgress } from "./AssessmentProgress";
import { QuestionRenderer, type QuestionValue } from "./QuestionRenderer";
import styles from "./AssessmentForm.module.css";

export interface AssessmentFormProps {
  data: StartAssessmentResponse;
  onComplete: (detail: AssessmentDetailResponse) => void;
  onError: (message: string) => void;
}

/**
 * In-conversation assessment wizard.
 * Each answer is submitted to the backend so progress persists there;
 * the final answer triggers completion and recommendations.
 */
export function AssessmentForm({ data, onComplete, onError }: AssessmentFormProps) {
  const questions = data.questions;
  const assessment = data.assessment;

  const [answers, setAnswers] = useState<Record<string, QuestionValue>>({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [progress, setProgress] = useState({
    answered: assessment.answeredQuestions,
    total: assessment.totalQuestions,
    percent: assessment.progressPercent,
  });
  const [submitting, setSubmitting] = useState(false);
  const [validationError, setValidationError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const current = questions[currentIndex];

  const setAnswer = useCallback((value: QuestionValue) => {
    setValidationError(null);
    setAnswers((prev) => ({ ...prev, [current?.id ?? ""]: value }));
  }, [current?.id]);

  const isEmpty = (value: QuestionValue): boolean => {
    if (value === null || value === undefined) return true;
    if (typeof value === "string") return value.trim() === "";
    if (Array.isArray(value)) return value.length === 0;
    return false;
  };

  const handleSubmit = async () => {
    if (!current) return;
    if (current.required && isEmpty(answers[current.id])) {
      setValidationError("Please provide an answer to continue.");
      return;
    }
    if (current.type === "number") {
      const num = answers[current.id];
      if (current.required && (num === null || Number.isNaN(Number(num)))) {
        setValidationError("Please enter a valid number.");
        return;
      }
    }

    setSubmitting(true);
    setSubmitError(null);
    try {
      const result: AnswerBatchResult = await assessmentsApi.submitAnswers(
        assessment.id,
        {
          answers: [{ questionId: current.id, value: answers[current.id] }],
        }
      );

      setProgress({
        answered: result.assessment.answeredQuestions,
        total: result.assessment.totalQuestions,
        percent: result.assessment.progressPercent,
      });

      if (currentIndex >= questions.length - 1) {
        const detail = await assessmentsApi.complete(assessment.id);
        onComplete(detail);
      } else {
        setCurrentIndex((i) => i + 1);
      }
    } catch (err) {
      const message =
        err instanceof ApiError
          ? err.message
          : "Unable to save your answer. Please try again.";
      setSubmitError(message);
      onError(message);
    } finally {
      setSubmitting(false);
    }
  };

  if (!current) return null;

  return (
    <div className={styles.form}>
      <AssessmentProgress
        answered={progress.answered}
        total={progress.total}
        progressPercent={progress.percent}
      />

      <div className={styles.questionCard}>
        <div className={styles.questionMeta}>
          Question {currentIndex + 1} of {questions.length}
        </div>
        <QuestionRenderer
          question={current}
          value={answers[current.id] ?? null}
          error={validationError ?? undefined}
          onChange={setAnswer}
        />

        {submitError && (
          <p className={styles.error} role="alert">
            {submitError}
          </p>
        )}

        <div className={styles.actions}>
          <Button
            type="button"
            variant="primary"
            loading={submitting}
            disabled={submitting}
            onClick={handleSubmit}
          >
            {currentIndex >= questions.length - 1 ? "Finish Assessment" : "Continue"}
          </Button>
        </div>
      </div>
    </div>
  );
}