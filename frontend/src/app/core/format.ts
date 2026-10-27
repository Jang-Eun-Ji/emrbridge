export function genderLabel(gender: string | null): string {
  if (gender === 'M') {
    return 'Male';
  }
  if (gender === 'F') {
    return 'Female';
  }
  return 'Unknown';
}

export function patientName(patient: {
  firstName: string | null;
  lastName: string | null;
}): string {
  const name = [patient.firstName, patient.lastName].filter(Boolean).join(' ');
  return name || 'Unnamed patient';
}

export function displayValue(value: string | number | null | undefined): string {
  if (value == null || value === '') {
    return '—';
  }
  return String(value);
}
