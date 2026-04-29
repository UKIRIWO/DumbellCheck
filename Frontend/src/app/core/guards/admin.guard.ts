import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthStateService } from '../services/auth-state.service';

export const adminGuard: CanActivateFn = () => {
  const authStateService = inject(AuthStateService);
  const router = inject(Router);

  if (!authStateService.ensureValidSession()) {
    return router.createUrlTree(['/auth/login']);
  }

  if (authStateService.isAdmin()) {
    return true;
  }

  return router.createUrlTree(['/app/feed']);
};
