import styles from "./AssessmentInputs.module.css";

export interface RadioGroupProps {
  id: string;
  label: string;
  value: string;
  options: string[];
  required?: boolean;
  onChange: (value: string) => void;
}

/** Radio button group for single-choice (select) questions. */
export function RadioGroup({
  id,
  label,
  value,
  options,
  required,
  onChange,
}: RadioGroupProps) {
  return (
    <div className={styles.field} role="radiogroup" aria-label={label}>
      <span className={styles.label}>
        {label}
        {required && <span className={styles.required} aria-hidden="true">*</span>}
      </span>
      <div className={styles.optionList}>
        {options.map((option) => (
          <label key={option} className={styles.option}>
            <input
              type="radio"
              name={id}
              value={option}
              checked={value === option}
              required={required}
              onChange={() => onChange(option)}
            />
            <span className={styles.optionLabel}>{option}</span>
          </label>
        ))}
      </div>
    </div>
  );
}