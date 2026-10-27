import { Routes } from '@angular/router';

import { authGuard, guestGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
  },
  {
    path: 'patients',
    canActivate: [authGuard],
    loadComponent: () => import('./features/patients/patient-list').then((m) => m.PatientList),
  },
  {
    path: 'patients/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/patients/patient-detail').then((m) => m.PatientDetail),
  },
  {
    path: 'conditions',
    canActivate: [authGuard],
    loadComponent: () => import('./features/conditions/conditions').then((m) => m.Conditions),
  },
  {
    path: 'pipeline',
    canActivate: [authGuard],
    loadComponent: () => import('./features/pipeline/pipeline').then((m) => m.Pipeline),
  },
  { path: '**', redirectTo: 'dashboard' },
];
