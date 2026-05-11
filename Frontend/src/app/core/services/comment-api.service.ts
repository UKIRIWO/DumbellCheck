import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import { Comment, CreateCommentRequest } from '../models/comment.model';
import { LikeToggleResponse } from '../models/like.model';

@Injectable({ providedIn: 'root' })
export class CommentApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  getComments(publicId: string): Observable<Comment[]> {
    return this.http
      .get<ApiResponse<Comment[]>>(
        `${this.apiBaseUrl}/publicaciones/${encodeURIComponent(publicId)}/comentarios`,
      )
      .pipe(map((r) => (r as { success: true; data: Comment[] }).data));
  }

  createComment(publicId: string, payload: CreateCommentRequest): Observable<Comment> {
    return this.http
      .post<ApiResponse<Comment>>(
        `${this.apiBaseUrl}/publicaciones/${encodeURIComponent(publicId)}/comentarios`,
        payload,
      )
      .pipe(map((r) => (r as { success: true; data: Comment }).data));
  }

  deleteComment(publicId: string, commentId: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(
        `${this.apiBaseUrl}/publicaciones/${encodeURIComponent(publicId)}/comentarios/${commentId}`,
      )
      .pipe(map(() => undefined));
  }

  toggleLike(publicId: string, commentId: number): Observable<LikeToggleResponse> {
    return this.http
      .post<ApiResponse<LikeToggleResponse>>(
        `${this.apiBaseUrl}/publicaciones/${encodeURIComponent(publicId)}/comentarios/${commentId}/like`,
        {},
      )
      .pipe(map((r) => (r as { success: true; data: LikeToggleResponse }).data));
  }
}
