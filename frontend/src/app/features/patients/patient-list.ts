import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, startWith, switchMap } from 'rxjs/operators';

import { AnalyticsApi } from '../../core/api/analytics-api';
import { load } from '../../core/api/load-state';
import { displayValue, genderLabel, patientName } from '../../core/format';
import { LoadError } from '../../shared/load-error/load-error';

@Component({
  selector: 'app-patient-list',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatTableModule,
    RouterLink,
    LoadError,
  ],
  templateUrl: './patient-list.html',
  styleUrl: './patient-list.scss',
})
export class PatientList {
  private readonly api = inject(AnalyticsApi);
  readonly query = new FormControl('', { nonNullable: true });
  readonly displayedColumns = ['patientId', 'name', 'gender', 'age', 'ageGroup', 'region'];
  readonly genderLabel = genderLabel;
  readonly patientName = patientName;
  readonly displayValue = displayValue;

  readonly state = toSignal(
    this.query.valueChanges.pipe(
      startWith(this.query.value),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((query) => load(this.api.listPatients(query))),
    ),
    { initialValue: { status: 'loading' as const } },
  );

  readonly patients = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? state.data.items : [];
  });

  readonly totalLabel = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? `${state.data.total.toLocaleString('ko-KR')}명` : '';
  });
}
