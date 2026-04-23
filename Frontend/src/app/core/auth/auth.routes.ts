import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login-page/login-page').then((m) => m.LoginPage),
  },
  {
    path: 'google/callback',
    loadComponent: () =>
      import('./pages/google-callback-page/google-callback-page').then((m) => m.GoogleCallbackPage),
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register-page/register-page').then((m) => m.RegisterPage),
  },
];
