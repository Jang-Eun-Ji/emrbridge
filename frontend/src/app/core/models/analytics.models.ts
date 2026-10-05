/** REST contract. Uses Spring Boot Jackson camelCase by default. */

export interface MonthlyVisitSummary {
  yearMonth: string;
  year: number;
  month: number;
  totalVisits: number;
  uniquePatients: number;
}

export interface DemographicSummary {
  ageGroup: string;
  gender: string;
  patientCount: number;
  visitCount: number;
}

export interface DiseaseSummary {
  diseaseCode: string | null;
  diseaseName: string;
  diseaseCategory: string | null;
  patientCount: number;
  visitCount: number;
}

export interface RegionSummary {
  state: string;
  city: string;
  regionName: string;
  patientCount: number;
  visitCount: number;
}

export interface DashboardSummary {
  totalPatients: number;
  totalVisits: number;
  totalDiseases: number;
  totalRegions: number;
  monthlyVisits: MonthlyVisitSummary[];
  demographics: DemographicSummary[];
  topDiseases: DiseaseSummary[];
  regions: RegionSummary[];
}

export interface PatientSummary {
  patientId: number;
  originalPatientId: number;
  firstName: string | null;
  lastName: string | null;
  gender: string | null;
  birthDate: string;
  age: number | null;
  ageGroup: string | null;
  regionName: string | null;
}

export interface PatientPage {
  items: PatientSummary[];
  total: number;
}

export interface PatientVisit {
  visitId: number;
  visitDate: string;
  visitType: string | null;
  diseaseCode: string | null;
  diseaseName: string;
  diseaseCategory: string | null;
}

export interface PatientDetail extends PatientSummary {
  state: string | null;
  city: string | null;
  registrationDate: string | null;
  visits: PatientVisit[];
}

export interface CategorySummary {
  diseaseCategory: string;
  patientCount: number;
  visitCount: number;
}

export interface ConditionAnalysis {
  categories: CategorySummary[];
  diseases: DiseaseSummary[];
}

export type PipelineStageKey = 'emr' | 'fhir' | 'analytics';
export type PipelineStageState = 'unknown' | 'idle' | 'running' | 'succeeded' | 'failed';

export interface PipelineStageStatus {
  key: PipelineStageKey;
  status: PipelineStageState;
  recordCount: number | null;
  lastRunAt: string | null;
  message: string | null;
}

export interface PipelineStatus {
  stages: PipelineStageStatus[];
}
