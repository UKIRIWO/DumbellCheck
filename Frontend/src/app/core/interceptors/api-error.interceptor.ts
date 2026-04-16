import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthStateService } from '../services/auth-state.service';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const authStateService = inject(AuthStateService);
  const router = inject(Router);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authStateService.clearSession();
        router.navigateByUrl('/auth/login');
      }

      return throwError(() => error);
    }),
  );
};
