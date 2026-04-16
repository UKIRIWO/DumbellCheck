import { Routes } from '@angular/router';

export const SUPPORT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/support-page/support-page.component').then((m) => m.SupportPageComponent),
  },
];
