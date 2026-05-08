import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import { CreatePostRequest, PostFeedItem, PageResponse, CursorPageResponse } from '../models/post.model';

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
    return this.fetchPagedFeed('publico', page, size);
  }

  getFeedAmigos(page = 0, size = 20): Observable<PageResponse<PostFeedItem>> {
    return this.fetchPagedFeed('amigos', page, size);
  }

  getFeedDescubrir(page = 0, size = 20): Observable<PageResponse<PostFeedItem>> {
    return this.fetchPagedFeed('descubrir', page, size);
  }

  private fetchPagedFeed(
    segment: 'publico' | 'amigos' | 'descubrir',
    page: number,
    size: number,
  ): Observable<PageResponse<PostFeedItem>> {
    return this.http
      .get<ApiResponse<PageResponse<PostFeedItem>>>(
        `${this.apiBaseUrl}/publicaciones/${segment}`,
        { params: { page: page.toString(), size: size.toString() } },
      )
      .pipe(map((r) => (r as { success: true; data: PageResponse<PostFeedItem> }).data));
  }

  getPostByPublicId(publicId: string): Observable<PostFeedItem> {
    return this.http
      .get<ApiResponse<PostFeedItem>>(`${this.apiBaseUrl}/publicaciones/${encodeURIComponent(publicId)}`)
      .pipe(map((r) => (r as { success: true; data: PostFeedItem }).data));
  }

  uploadMedia(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .post<ApiResponse<string>>(`${this.apiBaseUrl}/publicaciones/media`, formData)
      .pipe(map((r) => (r as { success: true; data: string }).data));
  }

  getPostsByUsername(username: string, cursor?: number, size = 18): Observable<CursorPageResponse<PostFeedItem>> {
    const params: Record<string, string> = { size: size.toString() };
    if (cursor !== undefined) {
      params['cursor'] = cursor.toString();
    }
    return this.http
      .get<ApiResponse<CursorPageResponse<PostFeedItem>>>(
        `${this.apiBaseUrl}/publicaciones/usuario/${username}`,
        { params },
      )
      .pipe(map((r) => (r as { success: true; data: CursorPageResponse<PostFeedItem> }).data));
  }
}
