"use client";

import { useState, useEffect, useRef, type ChangeEvent } from "react";
import styles from "./SearchInput.module.css";

export interface SearchInputProps {
  placeholder?: string;
  label?: string;
  defaultValue?: string;
  /** Debounce delay in ms. */
  debounceMs?: number;
  onChange: (value: string) => void;
}

/** Debounced search input for admin list pages. */
export function SearchInput({
  placeholder = "Search…",
  label,
  defaultValue = "",
  debounceMs = 400,
  onChange,
}: SearchInputProps) {
  const [value, setValue] = useState(defaultValue);
  const firstRun = useRef(true);

  useEffect(() => {
    if (firstRun.current) {
      firstRun.current = false;
      return;
    }
    const timer = setTimeout(() => {
      onChange(value);
    }, debounceMs);
    return () => clearTimeout(timer);
  }, [value, debounceMs, onChange]);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    setValue(e.target.value);
  };

  return (
    <div className={styles.wrapper}>
      {label && <span className={styles.label}>{label}</span>}
      <div className={styles.box}>
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
          type="search"
          className={styles.input}
          value={value}
          onChange={handleChange}
          placeholder={placeholder}
          aria-label={label || placeholder}
        />
      </div>
    </div>
  );
}