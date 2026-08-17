/* ================================================================
 * Validation utilities — client-side validation helpers.
 * These do NOT replace backend validation.
 * ================================================================ */

export function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export function isStrongPassword(password: string): boolean {
  return password.length >= 8;
}

export function passwordsMatch(password: string, confirm: string): boolean {
  return password === confirm;
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface LoginFormData {
  email: string;
  password: string;
}

export function validateLogin(data: LoginFormData): ValidationError[] {
  const errors: ValidationError[] = [];
  if (!data.email.trim()) {
    errors.push({ field: "email", message: "Email is required" });
  } else if (!isValidEmail(data.email)) {
    errors.push({ field: "email", message: "Please enter a valid email" });
  }
  if (!data.password) {
    errors.push({ field: "password", message: "Password is required" });
  }
  return errors;
}

export interface SignupFormData {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export function validateSignup(data: SignupFormData): ValidationError[] {
  const errors: ValidationError[] = [];
  if (!data.fullName.trim()) {
    errors.push({ field: "fullName", message: "Full name is required" });
  }
  if (!data.email.trim()) {
    errors.push({ field: "email", message: "Email is required" });
  } else if (!isValidEmail(data.email)) {
    errors.push({ field: "email", message: "Please enter a valid email" });
  }
  if (!data.password) {
    errors.push({ field: "password", message: "Password is required" });
  } else if (!isStrongPassword(data.password)) {
    errors.push({
      field: "password",
      message: "Password must be at least 8 characters",
    });
  }
  if (!data.confirmPassword) {
    errors.push({
      field: "confirmPassword",
      message: "Please confirm your password",
    });
  } else if (!passwordsMatch(data.password, data.confirmPassword)) {
    errors.push({
      field: "confirmPassword",
      message: "Passwords do not match",
    });
  }
  return errors;
}
