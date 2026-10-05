import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AnalyticsApi } from './analytics-api';

describe('AnalyticsApi', () => {
  let api: AnalyticsApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(AnalyticsApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('requests the dashboard summary', () => {
    api.getDashboard().subscribe();
    const request = http.expectOne('/api/analytics/dashboard');
    expect(request.request.method).toBe('GET');
    request.flush({
      totalPatients: 0,
      totalVisits: 0,
      totalDiseases: 0,
      totalRegions: 0,
      monthlyVisits: [],
      demographics: [],
      topDiseases: [],
      regions: [],
    });
  });

  it('sends a patient search query', () => {
    api.listPatients('Ann').subscribe();
    const request = http.expectOne('/api/analytics/patients?q=Ann');
    expect(request.request.method).toBe('GET');
    request.flush({ items: [], total: 0 });
  });
});
