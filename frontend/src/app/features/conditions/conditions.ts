import { DecimalPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { provideEchartsCore } from 'ngx-echarts';

import { AnalyticsApi } from '../../core/api/analytics-api';
import { echarts } from '../../core/charts/echarts-setup';
import { load } from '../../core/api/load-state';
import { categoryPieChart } from '../../core/charts/chart-options';
import { CategorySummary, ConditionAnalysis } from '../../core/models/analytics.models';
import { displayValue } from '../../core/format';
import { ChartPanel } from '../../shared/chart-panel/chart-panel';
import { LoadError } from '../../shared/load-error/load-error';

@Component({
  selector: 'app-conditions',
  imports: [DecimalPipe, MatProgressBarModule, MatTableModule, ChartPanel, LoadError],
  providers: [provideEchartsCore({ echarts })],
  templateUrl: './conditions.html',
  styleUrl: './conditions.scss',
})
export class Conditions {
  private readonly api = inject(AnalyticsApi);
  readonly displayedColumns = ['code', 'name', 'category', 'patients', 'visits'];
  readonly displayValue = displayValue;

  readonly state = toSignal(load(this.api.getConditions()), {
    initialValue: { status: 'loading' as const },
  });

  readonly diseases = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? state.data.diseases : [];
  });

  readonly categoryChart = computed(() => {
    const state = this.state();
    return state.status === 'ready' ? categoryPieChart(categoriesOf(state.data)) : null;
  });
}

function categoriesOf(analysis: ConditionAnalysis): CategorySummary[] {
  if (analysis.categories.length > 0) {
    return analysis.categories;
  }
  const grouped = new Map<string, CategorySummary>();
  for (const disease of analysis.diseases) {
    const name = disease.diseaseCategory || 'Other';
    const current = grouped.get(name) ?? {
      diseaseCategory: name,
      patientCount: 0,
      visitCount: 0,
    };
    current.patientCount += disease.patientCount;
    current.visitCount += disease.visitCount;
    grouped.set(name, current);
  }
  return [...grouped.values()];
}
