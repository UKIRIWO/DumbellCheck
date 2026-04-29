import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import { Ejercicio, GrupoMuscular } from '../models/exercise.model';

@Injectable({ providedIn: 'root' })
export class ExerciseApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getEjercicios(search?: string, grupoMuscularId?: number): Observable<Ejercicio[]> {
    let params = new HttpParams();
    if (search) params = params.set('search', search);
    if (grupoMuscularId != null) params = params.set('grupoMuscularId', grupoMuscularId.toString());

    return this.http
      .get<ApiResponse<Ejercicio[]>>(`${this.apiBaseUrl}/ejercicios`, { params })
      .pipe(map((r) => (r as { success: true; data: Ejercicio[] }).data));
  }

  getGruposMusculares(): Observable<GrupoMuscular[]> {
    return this.http
      .get<ApiResponse<GrupoMuscular[]>>(`${this.apiBaseUrl}/grupos-musculares`)
      .pipe(map((r) => (r as { success: true; data: GrupoMuscular[] }).data));
  }
}
