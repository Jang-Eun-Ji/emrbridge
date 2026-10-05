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
    subtitle: '원본 임상 데이터',
    description: 'Synthea CSV를 EMR DB에 적재합니다.',
    endpoint: 'POST /api/emr/import/import/all',
    icon: 'database',
  },
  {
    key: 'fhir',
    title: 'FHIR',
    subtitle: '표준 리소스 변환',
    description: 'EMR 레코드를 HAPI FHIR R4 리소스로 올립니다.',
    endpoint: 'POST /api/fhir/upload/all',
    icon: 'sync_alt',
  },
  {
    key: 'analytics',
    title: 'Analytics',
    subtitle: '스타 스키마 적재',
    description: 'FHIR에서 환자, 질병, 지역, 시간 차원과 방문 fact를 만듭니다.',
    endpoint: 'POST /api/analytics/etl/run',
    icon: 'analytics',
  },
];

const statusLabels: Record<PipelineStageState, string> = {
  unknown: '미확인',
  idle: '대기',
  running: '실행 중',
  succeeded: '완료',
  failed: '실패',
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
