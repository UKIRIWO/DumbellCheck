import { Routes } from '@angular/router';

export const FEED_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/feed-page/feed-page.component').then((m) => m.FeedPageComponent),
  },
];
