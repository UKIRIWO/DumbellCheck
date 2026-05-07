import { HttpClient, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize, map, Observable, of, shareReplay, switchMap, catchError } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { AuthSession } from '../models/auth-session.model';
import { AuthStateService } from '../services/auth-state.service';
import { API_BASE_URL } from '../tokens/api-base-url.token';

const SKIP_REFRESH_HEADER = 'x-skip-refresh';
let refreshInFlight$: Observable<string | null> | null = null;

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  if (request.headers.has(SKIP_REFRESH_HEADER)) {
    const cleanedRequest = request.clone({
      headers: request.headers.delete(SKIP_REFRESH_HEADER),
    });
    return next(cleanedRequest);
  }

  const authStateService = inject(AuthStateService);
  const http = inject(HttpClient);
  const apiBaseUrl = inject(API_BASE_URL);
  const isAuthEndpoint = request.url.includes('/auth/login')
    || request.url.includes('/auth/register')
    || request.url.includes('/auth/refresh');

  if (!authStateService.ensureValidSession()) {
    return next(request);
  }

  if (authStateService.hasValidAccessToken()) {
    const token = authStateService.accessToken();
    if (!token) {
      return next(request);
    }

    return next(addBearerToken(request, token));
  }

  if (!authStateService.hasValidRefreshToken() || isAuthEndpoint) {
    return next(request);
  }

  if (!refreshInFlight$) {
    const refreshToken = authStateService.getRefreshToken();
    if (!refreshToken) {
      authStateService.clearSession();
      return next(request);
    }

    refreshInFlight$ = http
      .post<ApiResponse<AuthSession>>(
        `${apiBaseUrl}/auth/refresh`,
        { refreshToken },
        {
          headers: {
            [SKIP_REFRESH_HEADER]: '1',
          },
        },
      )
      .pipe(
        map((response) => {
          if (response.success) {
            authStateService.setSession(response.data);
            return response.data.accessToken;
          }

          authStateService.clearSession();
          return null;
        }),
        catchError(() => {
          authStateService.clearSession();
          return of(null);
        }),
        finalize(() => {
          refreshInFlight$ = null;
        }),
        shareReplay(1),
      );
  }

  return refreshInFlight$.pipe(
    switchMap((token) => {
      if (!token) {
        return next(request);
      }

      return next(addBearerToken(request, token));
    }),
  );
};

function addBearerToken(request: HttpRequest<unknown>, token: string) {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });
}
