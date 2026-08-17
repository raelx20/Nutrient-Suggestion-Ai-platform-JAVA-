import { formatTime } from "@/lib/utils";
import styles from "./Messages.module.css";

export interface AssistantMessageProps {
  content: string;
  createdAt?: string;
  messageType?: string;
}

/** Left-aligned AI response with lightweight rich-text rendering. */
export function AssistantMessage({
  content,
  createdAt,
  messageType,
}: AssistantMessageProps) {
  const isStructured = messageType && messageType !== "text";

  return (
    <div className={styles.assistantRow}>
      <div
        className={`${styles.assistantBubble} ${
          isStructured ? styles.assistantStructured : ""
        }`}
      >
        <RichText content={content} />
      </div>
      {createdAt && (
        <span className={styles.timestamp}>{formatTime(createdAt)}</span>
      )}
    </div>
  );
}

/* ---- Lightweight, safe rich-text rendering (no dangerouslySetInnerHTML) ---- */

type Block =
  | { kind: "p"; text: string }
  | { kind: "ul"; items: string[] }
  | { kind: "ol"; items: string[] }
  | { kind: "heading"; text: string };

function parseBlocks(content: string): Block[] {
  const lines = content.split("\n");
  const blocks: Block[] = [];
  let list: { ordered: boolean; items: string[] } | null = null;

  const flushList = () => {
    if (list) {
      blocks.push(
        list.ordered
          ? { kind: "ol", items: list.items }
          : { kind: "ul", items: list.items }
      );
      list = null;
    }
  };

  for (const raw of lines) {
    const line = raw.trimEnd();
    if (!line.trim()) {
      flushList();
      continue;
    }
    const heading = line.match(/^#{1,3}\s+(.*)$/);
    const ul = line.match(/^[-*•]\s+(.*)$/);
    const ol = line.match(/^\d+[.)]\s+(.*)$/);
    if (heading) {
      flushList();
      blocks.push({ kind: "heading", text: heading[1] });
    } else if (ul) {
      if (!list || list.ordered) flushList();
      list = list ?? { ordered: false, items: [] };
      list.items.push(ul[1]);
    } else if (ol) {
      if (!list || !list.ordered) flushList();
      list = list ?? { ordered: true, items: [] };
      list.items.push(ol[1]);
    } else {
      flushList();
      blocks.push({ kind: "p", text: line });
    }
  }
  flushList();
  return blocks;
}

function InlineText({ text }: { text: string }) {
  /* Split on **bold** and *italic* markers. */
  const parts = text.split(/(\*\*[^*]+\*\*|\*[^*]+\*)/g);
  return (
    <>
      {parts.map((part, i) => {
        if (part.startsWith("**") && part.endsWith("**")) {
          return <strong key={i}>{part.slice(2, -2)}</strong>;
        }
        if (part.startsWith("*") && part.endsWith("*") && part.length > 2) {
          return <em key={i}>{part.slice(1, -1)}</em>;
        }
        return <span key={i}>{part}</span>;
      })}
    </>
  );
}

function RichText({ content }: { content: string }) {
  const blocks = parseBlocks(content);
  return (
    <div className={styles.richText}>
      {blocks.map((block, i) => {
        if (block.kind === "p") {
          return (
            <p key={i} className={styles.paragraph}>
              <InlineText text={block.text} />
            </p>
          );
        }
        if (block.kind === "heading") {
          return (
            <h3 key={i} className={styles.heading}>
              <InlineText text={block.text} />
            </h3>
          );
        }
        if (block.kind === "ul") {
          return (
            <ul key={i} className={styles.list}>
              {block.items.map((item, j) => (
                <li key={j}>
                  <InlineText text={item} />
                </li>
              ))}
            </ul>
          );
        }
        return (
          <ol key={i} className={styles.list}>
            {block.items.map((item, j) => (
              <li key={j}>
                <InlineText text={item} />
              </li>
            ))}
          </ol>
        );
      })}
    </div>
  );
}