import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import {
  AdminUser,
  AdminUserUpdateRequest,
  AdminPost,
  AdminComment,
  AdminBan,
  AdminBanCreateRequest,
  AdminBanUpdateRequest,
  AdminEjercicio,
  AdminEjercicioRequest,
  AdminPageResponse,
} from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  private data<T>(obs: Observable<ApiResponse<T>>): Observable<T> {
    return obs.pipe(map((r) => (r as { success: true; data: T }).data));
  }

  private pageParams(
    page: number,
    size: number,
    sortBy?: string | null,
    sortDir?: string | null,
  ): HttpParams {
    let p = new HttpParams().set('page', page).set('size', size);
    if (sortBy) p = p.set('sortBy', sortBy);
    if (sortDir) p = p.set('sortDir', sortDir);
    return p;
  }



  getUsers(
    page: number,
    size: number,
    q?: string,
    sortBy?: string | null,
    sortDir?: string | null,
  ): Observable<AdminPageResponse<AdminUser>> {
    let params = this.pageParams(page, size, sortBy, sortDir);
    if (q) params = params.set('q', q);
    return this.data(
      this.http.get<ApiResponse<AdminPageResponse<AdminUser>>>(
        `${this.apiBaseUrl}/admin/usuarios`,
        { params },
      ),
    );
  }

  updateUser(id: number, request: AdminUserUpdateRequest): Observable<AdminUser> {
    return this.data(
      this.http.put<ApiResponse<AdminUser>>(`${this.apiBaseUrl}/admin/usuarios/${id}`, request),
    );
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBaseUrl}/admin/usuarios/${id}`).pipe(map(() => undefined));
  }



  getPosts(
    page: number,
    size: number,
    sortBy?: string | null,
    sortDir?: string | null,
  ): Observable<AdminPageResponse<AdminPost>> {
    return this.data(
      this.http.get<ApiResponse<AdminPageResponse<AdminPost>>>(
        `${this.apiBaseUrl}/admin/publicaciones`,
        { params: this.pageParams(page, size, sortBy, sortDir) },
      ),
    );
  }

  togglePostActiva(id: number): Observable<AdminPost> {
    return this.data(
      this.http.put<ApiResponse<AdminPost>>(`${this.apiBaseUrl}/admin/publicaciones/${id}/estado`, {}),
    );
  }

  deletePost(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiBaseUrl}/admin/publicaciones/${id}`)
      .pipe(map(() => undefined));
  }



  getComments(
    page: number,
    size: number,
    sortBy?: string | null,
    sortDir?: string | null,
  ): Observable<AdminPageResponse<AdminComment>> {
    return this.data(
      this.http.get<ApiResponse<AdminPageResponse<AdminComment>>>(
        `${this.apiBaseUrl}/admin/comentarios`,
        { params: this.pageParams(page, size, sortBy, sortDir) },
      ),
    );
  }

  deleteComment(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiBaseUrl}/admin/comentarios/${id}`)
      .pipe(map(() => undefined));
  }



  getBans(
    page: number,
    size: number,
    sortBy?: string | null,
    sortDir?: string | null,
  ): Observable<AdminPageResponse<AdminBan>> {
    return this.data(
      this.http.get<ApiResponse<AdminPageResponse<AdminBan>>>(
        `${this.apiBaseUrl}/admin/baneos`,
        { params: this.pageParams(page, size, sortBy, sortDir) },
      ),
    );
  }

  createBan(request: AdminBanCreateRequest): Observable<AdminBan> {
    return this.data(
      this.http.post<ApiResponse<AdminBan>>(`${this.apiBaseUrl}/admin/baneos`, request),
    );
  }

  updateBan(id: number, request: AdminBanUpdateRequest): Observable<AdminBan> {
    return this.data(
      this.http.put<ApiResponse<AdminBan>>(`${this.apiBaseUrl}/admin/baneos/${id}`, request),
    );
  }

  deleteBan(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiBaseUrl}/admin/baneos/${id}`)
      .pipe(map(() => undefined));
  }



  getEjercicios(
    page: number,
    size: number,
    sortBy?: string | null,
    sortDir?: string | null,
  ): Observable<AdminPageResponse<AdminEjercicio>> {
    return this.data(
      this.http.get<ApiResponse<AdminPageResponse<AdminEjercicio>>>(
        `${this.apiBaseUrl}/admin/ejercicios`,
        { params: this.pageParams(page, size, sortBy, sortDir) },
      ),
    );
  }

  createEjercicio(request: AdminEjercicioRequest): Observable<AdminEjercicio> {
    return this.data(
      this.http.post<ApiResponse<AdminEjercicio>>(`${this.apiBaseUrl}/admin/ejercicios`, request),
    );
  }

  updateEjercicio(id: number, request: AdminEjercicioRequest): Observable<AdminEjercicio> {
    return this.data(
      this.http.put<ApiResponse<AdminEjercicio>>(
        `${this.apiBaseUrl}/admin/ejercicios/${id}`,
        request,
      ),
    );
  }

  deleteEjercicio(id: number): Observable<void> {
    return this.http
      .delete<void>(`${this.apiBaseUrl}/admin/ejercicios/${id}`)
      .pipe(map(() => undefined));
  }
}
