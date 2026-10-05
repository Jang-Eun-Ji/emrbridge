import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { AnalyticsApi } from '../../core/api/analytics-api';
import { LoadState, load } from '../../core/api/load-state';
import { displayValue, genderLabel, patientName } from '../../core/format';
import { PatientDetail as PatientDetailModel } from '../../core/models/analytics.models';
import { LoadError } from '../../shared/load-error/load-error';

@Component({
  selector: 'app-patient-detail',
  imports: [MatProgressBarModule, MatTableModule, RouterLink, LoadError],
  templateUrl: './patient-detail.html',
  styleUrl: './patient-detail.scss',
})
export class PatientDetail {
  private readonly api = inject(AnalyticsApi);
  private readonly route = inject(ActivatedRoute);
  readonly displayedColumns = ['visitDate', 'visitType', 'disease', 'category'];
  readonly genderLabel = genderLabel;
  readonly patientName = patientName;
  readonly displayValue = displayValue;

  readonly state = toSignal(
    this.route.paramMap.pipe(
      map((params) => Number(params.get('id'))),
      switchMap((patientId) => {
        if (!Number.isInteger(patientId)) {
          return of<LoadState<PatientDetailModel>>({ status: 'error', statusCode: 400 });
        }
        return load(this.api.getPatient(patientId));
      }),
    ),
    { initialValue: { status: 'loading' as const } },
  );

  readonly patient = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? state.data : null;
  });
}
