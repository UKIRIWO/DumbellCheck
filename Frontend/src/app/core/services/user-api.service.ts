import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../models/api-response.model';
import { SidebarData } from '../models/user-sidebar.model';
import { API_BASE_URL } from '../tokens/api-base-url.token';

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getSidebarData(limit = 6): Observable<SidebarData> {
    return this.http
      .get<ApiResponse<SidebarData>>(`${this.apiBaseUrl}/usuarios/me/sidebar`, {
        params: { limit: limit.toString() },
      })
      .pipe(map((r) => (r as { success: true; data: SidebarData }).data));
  }
}

