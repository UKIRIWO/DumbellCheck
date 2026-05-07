import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { RedirectCommand, Router } from '@angular/router';
import { AuthStateService } from '../../core/services/auth-state.service';

export const PROFILE_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [
      () => {
        const username = inject(AuthStateService).session()?.username;
        const router = inject(Router);
        if (username) {
          return new RedirectCommand(router.parseUrl(`/app/profile/${username}`));
        }
        return new RedirectCommand(router.parseUrl('/auth/login'));
      },
    ],
    // Dummy component required by the canActivate redirect pattern
    loadComponent: () =>
      import('./pages/profile-page/profile-page.component').then((m) => m.ProfilePageComponent),
  },
  {
    path: ':username',
    loadComponent: () =>
      import('./pages/profile-page/profile-page.component').then((m) => m.ProfilePageComponent),
  },
];
