import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { memberGuard } from './core/guards/member.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/landing/page/landing-page/landing-page').then((m) => m.LandingPage),
  },
  {
    path: 'auth',
    loadComponent: () =>
      import('./core/layout/public-layout/public-layout.component').then((m) => m.PublicLayoutComponent),
    canActivate: [guestGuard],
    loadChildren: () => import('./core/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: 'app',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./core/layout/private-layout/private-layout.component').then((m) => m.PrivateLayoutComponent),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'feed',
      },
      {
        path: 'feed',
        canActivate: [memberGuard],
        loadChildren: () => import('./features/feed/feed.routes').then((m) => m.FEED_ROUTES),
      },
      {
        path: 'profile',
        canActivate: [memberGuard],
        loadChildren: () => import('./features/profile/profile.routes').then((m) => m.PROFILE_ROUTES),
      },
      {
        path: 'chats',
        canActivate: [memberGuard],
        loadChildren: () => import('./features/chats/chats.routes').then((m) => m.CHATS_ROUTES),
      },
      {
        path: 'routines',
        canActivate: [memberGuard],
        loadChildren: () => import('./features/routines/routines.routes').then((m) => m.ROUTINES_ROUTES),
      },
      {
        path: 'stats',
        canActivate: [memberGuard],
        loadChildren: () => import('./features/stats/stats.routes').then((m) => m.STATS_ROUTES),
      },
      {
        path: 'workout/:publicId',
        canActivate: [memberGuard],
        loadComponent: () =>
          import('./features/feed/pages/workout-detail-page/workout-detail-page.component').then(
            (m) => m.WorkoutDetailPageComponent,
          ),
      },
      {
        path: 'admin',
        canActivate: [adminGuard],
        loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '/',
  },
];
