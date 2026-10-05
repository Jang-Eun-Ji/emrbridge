export function genderLabel(gender: string | null): string {
  if (gender === 'M') {
    return '남';
  }
  if (gender === 'F') {
    return '여';
  }
  return '미상';
}

export function patientName(patient: {
  firstName: string | null;
  lastName: string | null;
}): string {
  const name = [patient.firstName, patient.lastName].filter(Boolean).join(' ');
  return name || '이름 없음';
}

export function displayValue(value: string | number | null | undefined): string {
  if (value == null || value === '') {
    return '—';
  }
  return String(value);
}
