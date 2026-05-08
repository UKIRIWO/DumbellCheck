import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import { Perfil, UpdateProfileRequest, UsuarioEnlace, CreateLinkRequest, PerfilConnection } from '../models/profile.model';

@Injectable({ providedIn: 'root' })
export class ProfileApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getPerfil(username: string): Observable<Perfil> {
    return this.http
      .get<ApiResponse<Perfil>>(`${this.apiBaseUrl}/usuarios/${username}/perfil`)
      .pipe(map((r) => (r as { success: true; data: Perfil }).data));
  }

  updateMyProfile(request: UpdateProfileRequest): Observable<Perfil> {
    return this.http
      .put<ApiResponse<Perfil>>(`${this.apiBaseUrl}/usuarios/me/perfil`, request)
      .pipe(map((r) => (r as { success: true; data: Perfil }).data));
  }

  uploadFoto(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<ApiResponse<string>>(`${this.apiBaseUrl}/usuarios/me/foto`, formData)
      .pipe(map((r) => (r as { success: true; data: string }).data));
  }

  uploadBanner(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<ApiResponse<string>>(`${this.apiBaseUrl}/usuarios/me/banner`, formData)
      .pipe(map((r) => (r as { success: true; data: string }).data));
  }

  addEnlace(request: CreateLinkRequest): Observable<UsuarioEnlace> {
    return this.http
      .post<ApiResponse<UsuarioEnlace>>(`${this.apiBaseUrl}/usuarios/me/enlaces`, request)
      .pipe(map((r) => (r as { success: true; data: UsuarioEnlace }).data));
  }

  deleteEnlace(id: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.apiBaseUrl}/usuarios/me/enlaces/${id}`)
      .pipe(map(() => undefined));
  }

  seguirUsuario(username: string): Observable<Perfil> {
    return this.http
      .post<ApiResponse<Perfil>>(`${this.apiBaseUrl}/usuarios/${username}/seguir`, {})
      .pipe(map((r) => (r as { success: true; data: Perfil }).data));
  }

  dejarDeSeguirUsuario(username: string): Observable<Perfil> {
    return this.http
      .delete<ApiResponse<Perfil>>(`${this.apiBaseUrl}/usuarios/${username}/seguir`)
      .pipe(map((r) => (r as { success: true; data: Perfil }).data));
  }

  getSeguidores(username: string): Observable<PerfilConnection[]> {
    return this.http
      .get<ApiResponse<PerfilConnection[]>>(`${this.apiBaseUrl}/usuarios/${username}/seguidores`)
      .pipe(map((r) => (r as { success: true; data: PerfilConnection[] }).data));
  }

  getSeguidos(username: string): Observable<PerfilConnection[]> {
    return this.http
      .get<ApiResponse<PerfilConnection[]>>(`${this.apiBaseUrl}/usuarios/${username}/seguidos`)
      .pipe(map((r) => (r as { success: true; data: PerfilConnection[] }).data));
  }
}
