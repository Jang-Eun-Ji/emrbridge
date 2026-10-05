import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
  },
  {
    path: 'patients',
    loadComponent: () => import('./features/patients/patient-list').then((m) => m.PatientList),
  },
  {
    path: 'patients/:id',
    loadComponent: () =>
      import('./features/patients/patient-detail').then((m) => m.PatientDetail),
  },
  {
    path: 'conditions',
    loadComponent: () => import('./features/conditions/conditions').then((m) => m.Conditions),
  },
  {
    path: 'pipeline',
    loadComponent: () => import('./features/pipeline/pipeline').then((m) => m.Pipeline),
  },
  { path: '**', redirectTo: 'dashboard' },
];
