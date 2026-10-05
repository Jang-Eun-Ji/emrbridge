import { Component, input } from '@angular/core';
import type { EChartsCoreOption } from 'echarts/core';
import { NgxEchartsDirective } from 'ngx-echarts';

@Component({
  selector: 'app-chart-panel',
  imports: [NgxEchartsDirective],
  templateUrl: './chart-panel.html',
  styleUrl: './chart-panel.scss',
})
export class ChartPanel {
  readonly title = input.required<string>();
  readonly options = input<EChartsCoreOption | null>(null);
}
