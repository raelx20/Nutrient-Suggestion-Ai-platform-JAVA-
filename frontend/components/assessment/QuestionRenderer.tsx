import type { QuestionResponse } from "@/types";
import { NumberInput } from "./NumberInput";
import { TextInput } from "./TextInput";
import { BooleanInput } from "./BooleanInput";
import { RadioGroup } from "./RadioGroup";
import { CheckboxGroup } from "./CheckboxGroup";

export type QuestionValue = string | number | boolean | string[] | null;

export interface QuestionRendererProps {
  question: QuestionResponse;
  value: QuestionValue;
  error?: string;
  onChange: (value: QuestionValue) => void;
}

/**
 * Maps the backend question type to the appropriate control.
 * Types in production seed data: select, number, multi_select, boolean.
 * Unknown types fall back to a text input.
 */
export function QuestionRenderer({
  question,
  value,
  error,
  onChange,
}: QuestionRendererProps) {
  const id = `question-${question.key}`;
  const options = question.options ?? [];

  switch (question.type) {
    case "number":
      return (
        <NumberInput
          id={id}
          label={question.text}
          value={typeof value === "string" ? value : ""}
          required={question.required}
          error={error}
          onChange={(v) => onChange(v === "" ? null : Number(v))}
        />
      );

    case "select":
      return (
        <RadioGroup
          id={id}
          label={question.text}
          value={typeof value === "string" ? value : ""}
          options={options}
          required={question.required}
          onChange={(v) => onChange(v)}
        />
      );

    case "multi_select":
      return (
        <CheckboxGroup
          id={id}
          label={question.text}
          value={Array.isArray(value) ? value.map(String) : []}
          options={options}
          required={question.required}
          onChange={(v) => onChange(v)}
        />
      );

    case "boolean":
      return (
        <BooleanInput
          id={id}
          label={question.text}
          value={typeof value === "boolean" ? value : null}
          required={question.required}
          onChange={(v) => onChange(v)}
        />
      );

    case "text":
    default:
      return (
        <TextInput
          id={id}
          label={question.text}
          value={typeof value === "string" ? value : ""}
          required={question.required}
          error={error}
          multiline={question.text.length > 80}
          onChange={(v) => onChange(v)}
        />
      );
  }
}