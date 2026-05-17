import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthStateService } from '../services/auth-state.service';

export const memberGuard: CanActivateFn = () => {
  const authStateService = inject(AuthStateService);
  const router = inject(Router);

  if (authStateService.isAdmin()) {
    return router.createUrlTree(['/app/admin']);
  }
  return true;
};
