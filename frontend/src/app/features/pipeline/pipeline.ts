import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { AnalyticsApi } from '../../core/api/analytics-api';
import { load } from '../../core/api/load-state';
import { PipelineStageKey, PipelineStageState } from '../../core/models/analytics.models';
import { LoadError } from '../../shared/load-error/load-error';

interface PipelineStep {
  key: PipelineStageKey;
  title: string;
  subtitle: string;
  description: string;
  endpoint: string;
  icon: string;
}

const steps: PipelineStep[] = [
  {
    key: 'emr',
    title: 'EMR',
    subtitle: 'Source clinical data',
    description: 'Load Synthea CSV files into the EMR database.',
    endpoint: 'POST /api/emr/import/import/all',
    icon: 'database',
  },
  {
    key: 'fhir',
    title: 'FHIR',
    subtitle: 'Standard resource conversion',
    description: 'Upload EMR records as HAPI FHIR R4 resources.',
    endpoint: 'POST /api/fhir/upload/all',
    icon: 'sync_alt',
  },
  {
    key: 'analytics',
    title: 'Analytics',
    subtitle: 'Star schema load',
    description: 'Build patient, disease, region, and time dimensions plus visit facts from FHIR.',
    endpoint: 'POST /api/analytics/etl/run',
    icon: 'analytics',
  },
];

const statusLabels: Record<PipelineStageState, string> = {
  unknown: 'Unknown',
  idle: 'Idle',
  running: 'Running',
  succeeded: 'Succeeded',
  failed: 'Failed',
};

@Component({
  selector: 'app-pipeline',
  imports: [DatePipe, DecimalPipe, MatIconModule, MatProgressBarModule, LoadError],
  templateUrl: './pipeline.html',
  styleUrl: './pipeline.scss',
})
export class Pipeline {
  private readonly api = inject(AnalyticsApi);
  readonly state = toSignal(load(this.api.getPipeline()), {
    initialValue: { status: 'loading' as const },
  });

  readonly stages = computed(() => {
    const state = this.state();
    const reported = state.status === 'ready' ? state.data.stages : [];
    return steps.map((step) => {
      const match = reported.find((item) => item.key === step.key);
      return {
        ...step,
        status: match?.status ?? 'unknown',
        recordCount: match?.recordCount ?? null,
        lastRunAt: match?.lastRunAt ?? null,
        message: match?.message ?? null,
      };
    });
  });

  statusLabel(status: PipelineStageState): string {
    return statusLabels[status];
  }
}
