function onlyDigits(value: string) {
  return value.replace(/\D/g, '');
}

export function unformatCpf(value: string) {
  return onlyDigits(value).slice(0, 11);
}

export function formatCpf(value: string) {
  const digits = unformatCpf(value);
  return digits
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/^(\d{3})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3-$4');
}

export function unformatPhone(value: string) {
  return onlyDigits(value).slice(0, 11);
}

export function formatPhone(value: string) {
  const digits = unformatPhone(value);

  if (digits.length <= 10) {
    return digits
      .replace(/^(\d{2})(\d)/, '($1) $2')
      .replace(/(\d{4})(\d)/, '$1-$2');
  }

  return digits
    .replace(/^(\d{2})(\d)/, '($1) $2')
    .replace(/(\d{5})(\d)/, '$1-$2');
}

export function formatBirthDate(value: string) {
  return onlyDigits(value)
    .slice(0, 8)
    .replace(/^(\d{2})(\d)/, '$1/$2')
    .replace(/^(\d{2})\/(\d{2})(\d)/, '$1/$2/$3');
}

export function formatCep(value: string) {
  return onlyDigits(value)
    .slice(0, 8)
    .replace(/^(\d{5})(\d)/, '$1-$2');
}

export function unformatCep(value: string) {
  return onlyDigits(value).slice(0, 8);
}

export function isValidEmailFormat(value: string) {
  return /\S+@\S+\.\S+/.test(value.trim());
}
