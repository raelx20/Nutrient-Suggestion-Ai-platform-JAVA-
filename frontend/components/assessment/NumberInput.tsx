import styles from "./AssessmentInputs.module.css";

export interface NumberInputProps {
  id: string;
  label: string;
  value: string;
  required?: boolean;
  error?: string;
  onChange: (value: string) => void;
}

export function NumberInput({
  id,
  label,
  value,
  required,
  error,
  onChange,
}: NumberInputProps) {
  return (
    <div className={styles.field}>
      <label className={styles.label} htmlFor={id}>
        {label}
        {required && <span className={styles.required} aria-hidden="true">*</span>}
      </label>
      <input
        id={id}
        className={styles.input}
        type="number"
        inputMode="decimal"
        value={value}
        required={required}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-error` : undefined}
        onChange={(e) => onChange(e.target.value)}
      />
      {error && (
        <span id={`${id}-error`} className={styles.error} role="alert">
          {error}
        </span>
      )}
    </div>
  );
}