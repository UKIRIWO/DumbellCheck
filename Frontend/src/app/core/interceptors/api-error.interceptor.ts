import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { EMPTY, catchError, throwError } from 'rxjs';
import { AuthStateService } from '../services/auth-state.service';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const authStateService = inject(AuthStateService);
  const router = inject(Router);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // Auth endpoints handle their own errors (login, register, refresh)
      const isAuthEndpoint = request.url.includes('/auth/');

      if (error.status === 403 && error.error?.errorCode === 'USER_BANNED') {
        if (isAuthEndpoint) {
          // Let the login page handle it (show the ban modal itself)
          return throwError(() => error);
        }
        // Non-auth: already logged in but now banned — kick them out
        authStateService.clearSession();
        router.navigate(['/auth/login'], {
          state: { banData: error.error?.data ?? null },
        });
        return EMPTY;
      }

      if (error.status === 401 && !isAuthEndpoint) {
        authStateService.clearSession();
        router.navigateByUrl('/auth/login');
        return EMPTY;
      }

      return throwError(() => error);
    }),
  );
};
