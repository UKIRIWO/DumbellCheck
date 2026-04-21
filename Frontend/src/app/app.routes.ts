import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
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
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
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
        loadChildren: () => import('./features/feed/feed.routes').then((m) => m.FEED_ROUTES),
      },
      {
        path: 'profile',
        loadChildren: () => import('./features/profile/profile.routes').then((m) => m.PROFILE_ROUTES),
      },
      {
        path: 'chats',
        loadChildren: () => import('./features/chats/chats.routes').then((m) => m.CHATS_ROUTES),
      },
      {
        path: 'notifications',
        loadChildren: () =>
          import('./features/notifications/notifications.routes').then((m) => m.NOTIFICATIONS_ROUTES),
      },
      {
        path: 'routines',
        loadChildren: () => import('./features/routines/routines.routes').then((m) => m.ROUTINES_ROUTES),
      },
      {
        path: 'support',
        loadChildren: () => import('./features/support/support.routes').then((m) => m.SUPPORT_ROUTES),
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
