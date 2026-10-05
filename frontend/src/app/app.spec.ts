import { provideRouter, withDisabledInitialNavigation } from '@angular/router';
import { TestBed } from '@angular/core/testing';

import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes, withDisabledInitialNavigation())],
    }).compileComponents();
  });

  it('shows the four sections', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(text).toContain('Dashboard');
    expect(text).toContain('Patients');
    expect(text).toContain('Conditions');
    expect(text).toContain('Data Pipeline');
  });
});
