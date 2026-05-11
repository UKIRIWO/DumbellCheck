import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import { CreateRoutineRequest, Routine, RoutineListItem } from '../models/routine.model';

@Injectable({ providedIn: 'root' })
export class RoutineApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getMyRutinas(): Observable<RoutineListItem[]> {
    return this.http
      .get<ApiResponse<RoutineListItem[]>>(`${this.apiBaseUrl}/rutinas/mias`)
      .pipe(map((r) => (r as { success: true; data: RoutineListItem[] }).data));
  }

  getPublicRutinas(): Observable<RoutineListItem[]> {
    return this.http
      .get<ApiResponse<RoutineListItem[]>>(`${this.apiBaseUrl}/rutinas`)
      .pipe(map((r) => (r as { success: true; data: RoutineListItem[] }).data));
  }

  getRutina(publicId: string): Observable<Routine> {
    return this.http
      .get<ApiResponse<Routine>>(`${this.apiBaseUrl}/rutinas/${encodeURIComponent(publicId)}`)
      .pipe(map((r) => (r as { success: true; data: Routine }).data));
  }

  createRutina(payload: CreateRoutineRequest): Observable<Routine> {
    return this.http
      .post<ApiResponse<Routine>>(`${this.apiBaseUrl}/rutinas`, payload)
      .pipe(map((r) => (r as { success: true; data: Routine }).data));
  }

  updateRutina(publicId: string, payload: CreateRoutineRequest): Observable<Routine> {
    return this.http
      .put<ApiResponse<Routine>>(`${this.apiBaseUrl}/rutinas/${encodeURIComponent(publicId)}`, payload)
      .pipe(map((r) => (r as { success: true; data: Routine }).data));
  }

  deleteRutina(publicId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.apiBaseUrl}/rutinas/${encodeURIComponent(publicId)}`)
      .pipe(map(() => undefined));
  }
}
