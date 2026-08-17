import styles from "./AssessmentInputs.module.css";

export interface BooleanInputProps {
  id: string;
  label: string;
  value: boolean | null;
  required?: boolean;
  onChange: (value: boolean) => void;
}

/** Yes/No toggle for boolean questions. */
export function BooleanInput({
  id,
  label,
  value,
  required,
  onChange,
}: BooleanInputProps) {
  return (
    <div className={styles.field} role="radiogroup" aria-label={label}>
      <span className={styles.label}>
        {label}
        {required && <span className={styles.required} aria-hidden="true">*</span>}
      </span>
      <div className={styles.segmented}>
        <button
          type="button"
          id={`${id}-yes`}
          className={value === true ? styles.segmentActive : styles.segment}
          aria-pressed={value === true}
          onClick={() => onChange(true)}
        >
          Yes
        </button>
        <button
          type="button"
          id={`${id}-no`}
          className={value === false ? styles.segmentActive : styles.segment}
          aria-pressed={value === false}
          onClick={() => onChange(false)}
        >
          No
        </button>
      </div>
    </div>
  );
}