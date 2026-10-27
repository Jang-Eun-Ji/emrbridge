import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { provideEchartsCore } from 'ngx-echarts';

import { AnalyticsApi } from '../../core/api/analytics-api';
import { echarts } from '../../core/charts/echarts-setup';
import { load } from '../../core/api/load-state';
import {
  demographicChart,
  diseaseBarChart,
  monthlyVisitChart,
  regionBarChart,
} from '../../core/charts/chart-options';
import { ChartPanel } from '../../shared/chart-panel/chart-panel';
import { LoadError } from '../../shared/load-error/load-error';

@Component({
  selector: 'app-dashboard',
  imports: [MatProgressBarModule, ChartPanel, LoadError],
  providers: [provideEchartsCore({ echarts })],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly api = inject(AnalyticsApi);
  readonly state = toSignal(load(this.api.getDashboard()), {
    initialValue: { status: 'loading' },
  });

  readonly kpis = computed(() => {
    const state = this.state();
    const data = state.status === 'ready' ? state.data : null;
    const value = (count: number | undefined) =>
      count == null ? '—' : count.toLocaleString('en-US');
    return [
      { label: 'Patients', value: value(data?.totalPatients) },
      { label: 'Visits', value: value(data?.totalVisits) },
      { label: 'Diseases', value: value(data?.totalDiseases) },
      { label: 'Regions', value: value(data?.totalRegions) },
    ];
  });

  readonly monthlyChart = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? monthlyVisitChart(state.data.monthlyVisits) : null;
  });

  readonly demographicChart = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? demographicChart(state.data.demographics) : null;
  });

  readonly diseaseChart = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? diseaseBarChart(state.data.topDiseases) : null;
  });

  readonly regionChart = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? regionBarChart(state.data.regions) : null;
  });
}
