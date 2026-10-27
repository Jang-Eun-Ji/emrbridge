import type { EChartsCoreOption } from 'echarts/core';

import {
  CategorySummary,
  DemographicSummary,
  DiseaseSummary,
  MonthlyVisitSummary,
  RegionSummary,
} from '../models/analytics.models';

const palette = ['#0f6e8c', '#1f8a70', '#d97706', '#b4534a', '#3d5a80', '#6b7280'];

export function monthlyVisitChart(rows: MonthlyVisitSummary[]): EChartsCoreOption | null {
  if (rows.length === 0) {
    return null;
  }
  return {
    color: [palette[0]],
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, top: 24, bottom: 32 },
    xAxis: { type: 'category', data: rows.map((row) => row.yearMonth) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: 'Visits',
        type: 'line',
        smooth: true,
        showSymbol: false,
        areaStyle: { opacity: 0.12 },
        data: rows.map((row) => row.totalVisits),
      },
    ],
  };
}

export function demographicChart(rows: DemographicSummary[]): EChartsCoreOption | null {
  if (rows.length === 0) {
    return null;
  }
  const ageGroups = [...new Set(rows.map((row) => row.ageGroup))];
  const seriesFor = (gender: string, name: string) => ({
    name,
    type: 'bar' as const,
    data: ageGroups.map(
      (ageGroup) =>
        rows.find((row) => row.ageGroup === ageGroup && row.gender === gender)?.patientCount ?? 0,
    ),
  });
  return {
    color: [palette[0], palette[1]],
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 48, right: 16, top: 36, bottom: 32 },
    xAxis: { type: 'category', data: ageGroups },
    yAxis: { type: 'value', minInterval: 1 },
    series: [seriesFor('M', 'Male'), seriesFor('F', 'Female')],
  };
}

export function rankedBarChart(
  rows: { label: string; value: number }[],
  seriesName: string,
): EChartsCoreOption | null {
  if (rows.length === 0) {
    return null;
  }
  const ranked = [...rows].sort((a, b) => b.value - a.value).slice(0, 8).reverse();
  return {
    color: [palette[0]],
    tooltip: { trigger: 'axis' },
    grid: { left: 120, right: 24, top: 16, bottom: 24 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: ranked.map((row) => row.label) },
    series: [{ name: seriesName, type: 'bar', data: ranked.map((row) => row.value) }],
  };
}

export function diseaseBarChart(rows: DiseaseSummary[]): EChartsCoreOption | null {
  return rankedBarChart(
    rows.map((row) => ({ label: row.diseaseName, value: row.visitCount })),
    'Visits',
  );
}

export function regionBarChart(rows: RegionSummary[]): EChartsCoreOption | null {
  return rankedBarChart(
    rows.map((row) => ({ label: row.regionName, value: row.visitCount })),
    'Visits',
  );
}

export function categoryPieChart(rows: CategorySummary[]): EChartsCoreOption | null {
  if (rows.length === 0) {
    return null;
  }
  return {
    color: palette,
    tooltip: { trigger: 'item' },
    legend: { type: 'scroll', bottom: 0 },
    series: [
      {
        name: 'Category',
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        data: rows.map((row) => ({ name: row.diseaseCategory, value: row.visitCount })),
      },
    ],
  };
}
