import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from '../tokens/api-base-url.token';
import { ApiResponse } from '../models/api-response.model';
import {
  ChatListItem,
  ChatDetail,
  ChatMessage,
  CreateGroupChatRequest,
  AddGroupMembersRequest,
  SendMessageRequest,
  UpdateMessageRequest,
  ChatSearchUser,
  CursorPageResponse,
  UpdateChatRequest,
  UpdateParticipantRoleRequest,
} from '../models/chat.model';

@Injectable({ providedIn: 'root' })
export class ChatApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  private data<T>(obs: Observable<ApiResponse<T>>): Observable<T> {
    return obs.pipe(map((r) => (r as { success: true; data: T }).data));
  }

  getMisChats(): Observable<ChatListItem[]> {
    return this.data(this.http.get<ApiResponse<ChatListItem[]>>(`${this.apiBaseUrl}/chats`));
  }

  findOrCreateDirectChat(username: string): Observable<ChatDetail> {
    return this.data(
      this.http.post<ApiResponse<ChatDetail>>(
        `${this.apiBaseUrl}/chats/directo/${encodeURIComponent(username)}`,
        {},
      ),
    );
  }

  createGroupChat(request: CreateGroupChatRequest): Observable<ChatDetail> {
    return this.data(
      this.http.post<ApiResponse<ChatDetail>>(`${this.apiBaseUrl}/chats/grupo`, request),
    );
  }

  getChatDetail(publicId: string): Observable<ChatDetail> {
    return this.data(
      this.http.get<ApiResponse<ChatDetail>>(`${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}`),
    );
  }

  getMensajes(
    publicId: string,
    cursor?: number | null,
    size = 30,
  ): Observable<CursorPageResponse<ChatMessage>> {
    let params = new HttpParams().set('size', size);
    if (cursor != null) params = params.set('cursor', cursor);
    return this.data(
      this.http.get<ApiResponse<CursorPageResponse<ChatMessage>>>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/mensajes`,
        { params },
      ),
    );
  }

  sendMessage(publicId: string, request: SendMessageRequest): Observable<ChatMessage> {
    return this.data(
      this.http.post<ApiResponse<ChatMessage>>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/mensajes`,
        request,
      ),
    );
  }

  uploadMedia(publicId: string, file: File): Observable<string> {
    const form = new FormData();
    form.append('file', file);
    return this.data(
      this.http.post<ApiResponse<string>>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/mensajes/media`,
        form,
      ),
    );
  }

  uploadGroupPhoto(publicId: string, file: File): Observable<ChatDetail> {
    const form = new FormData();
    form.append('file', file);
    return this.data(
      this.http.post<ApiResponse<ChatDetail>>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/foto`,
        form,
      ),
    );
  }

  editMessage(
    publicId: string,
    mensajeId: number,
    request: UpdateMessageRequest,
  ): Observable<ChatMessage> {
    return this.data(
      this.http.put<ApiResponse<ChatMessage>>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/mensajes/${mensajeId}`,
        request,
      ),
    );
  }

  deleteMessage(publicId: string, mensajeId: number): Observable<void> {
    return this.http
      .delete<void>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/mensajes/${mensajeId}`,
      )
      .pipe(map(() => undefined));
  }

  updateLastSeen(publicId: string): Observable<void> {
    return this.http
      .put<void>(`${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/vista`, {})
      .pipe(map(() => undefined));
  }

  searchUsers(q?: string, page = 0, size = 20): Observable<ChatSearchUser[]> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (q) params = params.set('q', q);
    return this.data(
      this.http.get<ApiResponse<ChatSearchUser[]>>(`${this.apiBaseUrl}/chats/usuarios`, { params }),
    );
  }

  updateChat(publicId: string, request: UpdateChatRequest): Observable<ChatDetail> {
    return this.data(
      this.http.put<ApiResponse<ChatDetail>>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}`,
        request,
      ),
    );
  }

  addGroupMembers(publicId: string, request: AddGroupMembersRequest): Observable<ChatDetail> {
    return this.data(
      this.http.post<ApiResponse<ChatDetail>>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/participantes`,
        request,
      ),
    );
  }

  updateParticipantRole(
    publicId: string,
    usuarioId: number,
    request: UpdateParticipantRoleRequest,
  ): Observable<ChatDetail> {
    return this.data(
      this.http.patch<ApiResponse<ChatDetail>>(
        `${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/participantes/${usuarioId}/rol`,
        request,
      ),
    );
  }

  leaveChat(publicId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.apiBaseUrl}/chats/${encodeURIComponent(publicId)}/salir`)
      .pipe(map(() => undefined));
  }
}
