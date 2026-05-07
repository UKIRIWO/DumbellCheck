import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthStateService } from '../services/auth-state.service';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const authStateService = inject(AuthStateService);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authStateService.clearSession();
      }

      return throwError(() => error);
    }),
  );
};
