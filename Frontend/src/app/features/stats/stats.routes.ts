import { Routes } from '@angular/router';

export const STATS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/stats-page/stats-page').then((m) => m.StatsPage),
  },
];
