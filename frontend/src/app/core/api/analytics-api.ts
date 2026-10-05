import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import {
  ConditionAnalysis,
  DashboardSummary,
  PatientDetail,
  PatientPage,
  PipelineStatus,
} from '../models/analytics.models';

@Injectable({ providedIn: 'root' })
export class AnalyticsApi {
  private readonly http = inject(HttpClient);

  getDashboard(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>('/api/analytics/dashboard');
  }

  listPatients(query: string): Observable<PatientPage> {
    const params = query ? { q: query } : undefined;
    return this.http.get<PatientPage>('/api/analytics/patients', { params });
  }

  getPatient(patientId: number): Observable<PatientDetail> {
    return this.http.get<PatientDetail>(`/api/analytics/patients/${patientId}`);
  }

  getConditions(): Observable<ConditionAnalysis> {
    return this.http.get<ConditionAnalysis>('/api/analytics/conditions');
  }

  getPipeline(): Observable<PipelineStatus> {
    return this.http.get<PipelineStatus>('/api/analytics/pipeline');
  }
}
