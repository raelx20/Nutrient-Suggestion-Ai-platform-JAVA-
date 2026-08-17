import styles from "./AssessmentInputs.module.css";

export interface CheckboxGroupProps {
  id: string;
  label: string;
  value: string[];
  options: string[];
  required?: boolean;
  onChange: (value: string[]) => void;
}

/** Checkbox group for multi-select questions. */
export function CheckboxGroup({
  id,
  label,
  value,
  options,
  required,
  onChange,
}: CheckboxGroupProps) {
  const toggle = (option: string) => {
    if (value.includes(option)) {
      onChange(value.filter((v) => v !== option));
    } else {
      onChange([...value, option]);
    }
  };

  return (
    <div className={styles.field} role="group" aria-label={label}>
      <span className={styles.label}>
        {label}
        {required && <span className={styles.required} aria-hidden="true">*</span>}
      </span>
      <div className={styles.optionList}>
        {options.map((option) => (
          <label key={option} className={styles.option}>
            <input
              type="checkbox"
              name={id}
              checked={value.includes(option)}
              required={required && value.length === 0}
              onChange={() => toggle(option)}
            />
            <span className={styles.optionLabel}>{option}</span>
          </label>
        ))}
      </div>
    </div>
  );
}