import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import { StatsData } from '../models/stats.model';

@Injectable({ providedIn: 'root' })
export class StatsApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getMyStats(months: number): Observable<StatsData> {
    return this.http
      .get<ApiResponse<StatsData>>(`${this.apiBaseUrl}/estadisticas/me?months=${months}`)
      .pipe(map((r) => (r as { success: true; data: StatsData }).data));
  }
}
