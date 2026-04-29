import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStateService } from '../services/auth-state.service';

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const authStateService = inject(AuthStateService);

  if (!authStateService.ensureValidSession()) {
    return next(request);
  }

  const token = authStateService.accessToken();

  if (!token) {
    return next(request);
  }

  const authenticatedRequest = request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authenticatedRequest);
};
