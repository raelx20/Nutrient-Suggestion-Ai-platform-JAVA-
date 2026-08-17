/* ================================================================
 * Consumer experience configuration.
 * Welcome copy and assessment messaging are configurable so they
 * can later be driven by backend settings (Admin → Settings).
 * ================================================================ */

export const consumerConfig = {
  appName: process.env.NEXT_PUBLIC_APP_NAME ?? "VitalEdge",

  /** Large centered headline on the initial chat screen. */
  welcomeHeadline: "How may I help you today?",

  /** Subheading under the welcome headline. */
  welcomeSubtext:
    "I'm your personal nutrition assistant. Tell me what you'd like help with.",

  /** Placeholder text in the chat composer. */
  composerPlaceholder: "Ask anything about nutrition, health, or supplements…",

  /** Shown above the assessment start button. */
  assessmentWarningTitle: "A quick assessment",

  /** Pre-assessment guidance (backend/configuration driven). */
  assessmentWarningBody:
    "These questions are important for creating your personalized nutrition assessment. Please answer honestly and as accurately as possible. If you are unsure about an answer, or if you have a complex medical situation, please contact a counsellor.",

  assessmentWarningCta: "Continue",
};