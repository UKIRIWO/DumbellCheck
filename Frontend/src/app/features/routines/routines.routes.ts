import { Routes } from '@angular/router';

export const ROUTINES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/routines-page/routines-page.component').then((m) => m.RoutinesPageComponent),
  },
];
