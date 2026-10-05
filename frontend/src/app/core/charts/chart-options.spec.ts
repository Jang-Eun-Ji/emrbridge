import { diseaseBarChart, monthlyVisitChart } from './chart-options';

describe('chart options', () => {
  it('skips a chart when there are no rows', () => {
    expect(monthlyVisitChart([])).toBeNull();
  });

  it('builds a monthly visit series', () => {
    const options = monthlyVisitChart([
      { yearMonth: '2024-03', year: 2024, month: 3, totalVisits: 12, uniquePatients: 8 },
    ]);
    const series = options?.['series'] as { data: number[] }[];
    expect(series[0].data).toEqual([12]);
  });

  it('keeps the highest disease counts', () => {
    const options = diseaseBarChart([
      {
        diseaseCode: 'A',
        diseaseName: 'Alpha',
        diseaseCategory: 'Other',
        patientCount: 1,
        visitCount: 2,
      },
      {
        diseaseCode: 'B',
        diseaseName: 'Beta',
        diseaseCategory: 'Other',
        patientCount: 3,
        visitCount: 9,
      },
    ]);
    const axis = options?.['yAxis'] as { data: string[] };
    expect(axis.data).toEqual(['Alpha', 'Beta']);
  });
});
