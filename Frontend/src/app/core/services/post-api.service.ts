import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import { CreatePostRequest, PostFeedItem, PageResponse } from '../models/post.model';

@Injectable({ providedIn: 'root' })
export class PostApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  createPost(payload: CreatePostRequest): Observable<PostFeedItem> {
    return this.http
      .post<ApiResponse<PostFeedItem>>(`${this.apiBaseUrl}/publicaciones`, payload)
      .pipe(map((r) => (r as { success: true; data: PostFeedItem }).data));
  }

  getFeedPublico(page = 0, size = 20): Observable<PageResponse<PostFeedItem>> {
    return this.http
      .get<ApiResponse<PageResponse<PostFeedItem>>>(
        `${this.apiBaseUrl}/publicaciones/publico`,
        { params: { page: page.toString(), size: size.toString() } },
      )
      .pipe(map((r) => (r as { success: true; data: PageResponse<PostFeedItem> }).data));
  }

  getPostById(id: number): Observable<PostFeedItem> {
    return this.http
      .get<ApiResponse<PostFeedItem>>(`${this.apiBaseUrl}/publicaciones/${id}`)
      .pipe(map((r) => (r as { success: true; data: PostFeedItem }).data));
  }

  uploadMedia(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<ApiResponse<string>>(`${this.apiBaseUrl}/publicaciones/media`, formData)
      .pipe(map((r) => (r as { success: true; data: string }).data));
  }
}
