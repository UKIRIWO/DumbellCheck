import { Routes } from '@angular/router';

export const ROUTINES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/routines-page/routines-page.component').then((m) => m.RoutinesPageComponent),
  },
  {
    path: 'crear',
    loadComponent: () =>
      import('./pages/routine-form-page/routine-form-page.component').then((m) => m.RoutineFormPageComponent),
  },
  {
    path: ':publicId',
    loadComponent: () =>
      import('./pages/routine-detail-page/routine-detail-page.component').then(
        (m) => m.RoutineDetailPageComponent,
      ),
  },
  {
    path: ':publicId/editar',
    loadComponent: () =>
      import('./pages/routine-form-page/routine-form-page.component').then((m) => m.RoutineFormPageComponent),
  },
];
