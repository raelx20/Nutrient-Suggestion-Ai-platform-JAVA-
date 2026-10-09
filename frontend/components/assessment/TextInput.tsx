import styles from "./AssessmentInputs.module.css";

export interface TextInputProps {
  id: string;
  label: string;
  value: string;
  required?: boolean;
  error?: string;
  multiline?: boolean;
  placeholder?: string;
  onChange: (value: string) => void;
}

export function TextInput({
  id,
  label,
  value,
  required,
  error,
  multiline = false,
  placeholder,
  onChange,
}: TextInputProps) {
  const common = {
    id,
    className: `${styles.input} ${multiline ? styles.textarea : ""}`,
    value,
    required,
    placeholder,
    "aria-invalid": !!error,
    "aria-describedby": error ? `${id}-error` : undefined,
    onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
      onChange(e.target.value),
  };

  return (
    <div className={styles.field}>
      <label className={styles.label} htmlFor={id}>
        {label}
        {required && <span className={styles.required} aria-hidden="true">*</span>}
      </label>
      {multiline ? (
        <textarea {...common} rows={4} />
      ) : (
        <input type="text" {...common} />
      )}
      {error && (
        <span id={`${id}-error`} className={styles.error} role="alert">
          {error}
        </span>
      )}
    </div>
  );
}