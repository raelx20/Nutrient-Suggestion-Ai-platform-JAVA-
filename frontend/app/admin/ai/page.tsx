"use client";

import { AdminPageHeader, AwaitingBackend } from "@/components/admin";
import styles from "../admin-pages.module.css";

export default function AdminAiPage() {
  return (
    <div className={styles.page}>
      <AdminPageHeader
        title="AI Control Center"
        description="Provider status, model configuration, usage, token metrics, prompts, and AI logs."
      />
      <AwaitingBackend
        endpoint="GET /api/v1/admin/ai/status, /ai/config, /ai/logs"
        description="AI provider status, current model, usage/latency/token metrics, configuration, prompt management, and logs will render here once the backend exposes the AI endpoints. API keys will always be masked."
      />
    </div>
  );
}