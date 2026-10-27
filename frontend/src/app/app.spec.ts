import { signal } from '@angular/core';
import { provideRouter, withDisabledInitialNavigation } from '@angular/router';
import { TestBed } from '@angular/core/testing';

import { App } from './app';
import { routes } from './app.routes';
import { AuthService } from './core/auth/auth.service';
import { AuthUser } from './core/models/auth.models';

describe('App', () => {
  beforeEach(async () => {
    const user = signal<AuthUser | null>({
      id: 1,
      username: 'admin',
      role: 'ADMIN',
      organizationSyntheaId: null,
      hospitalName: null,
      admin: true,
    });

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter(routes, withDisabledInitialNavigation()),
        {
          provide: AuthService,
          useValue: {
            user: user.asReadonly(),
            isLoggedIn: signal(true).asReadonly(),
            isAdmin: signal(true).asReadonly(),
            logout: () => undefined,
          },
        },
      ],
    }).compileComponents();
  });

  it('shows the four sections when signed in', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(text).toContain('Dashboard');
    expect(text).toContain('Patients');
    expect(text).toContain('Conditions');
    expect(text).toContain('Data Pipeline');
  });
});
