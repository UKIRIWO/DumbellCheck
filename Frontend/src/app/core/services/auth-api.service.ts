import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { AuthSession } from '../models/auth-session.model';
import { API_BASE_URL } from '../tokens/api-base-url.token';

export interface RegisterUserRequest {
  username: string;
  email: string;
  nombre: string;
  apellido1: string;
  apellido2?: string;
  password: string;
}

export interface LoginRequest {
  principal: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  register(payload: RegisterUserRequest): Observable<ApiResponse<unknown>> {
    return this.http.post<ApiResponse<unknown>>(`${this.apiBaseUrl}/auth/register`, payload);
  }

  login(payload: LoginRequest): Observable<ApiResponse<AuthSession>> {
    return this.http.post<ApiResponse<AuthSession>>(`${this.apiBaseUrl}/auth/login`, payload);
  }
}
