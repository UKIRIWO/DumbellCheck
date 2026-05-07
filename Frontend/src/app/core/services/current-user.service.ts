import { Injectable, inject, signal } from '@angular/core';
import { Observable, shareReplay, tap, catchError, throwError } from 'rxjs';
import { SidebarData, SidebarProfile, SidebarSuggestion } from '../models/user-sidebar.model';
import { UserApiService } from './user-api.service';

@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  private readonly userApi = inject(UserApiService);

  readonly profile = signal<SidebarProfile | null>(null);
  readonly suggestions = signal<SidebarSuggestion[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  private cached$: Observable<SidebarData> | null = null;

  ensureLoaded(limit = 8): Observable<SidebarData> {
    if (!this.cached$) {
      this.loading.set(true);
      this.errorMessage.set('');
      this.cached$ = this.userApi.getSidebarData(limit).pipe(
        tap((data) => {
          this.profile.set(data.perfil);
          this.suggestions.set(data.sugerencias);
          this.loading.set(false);
        }),
        catchError((err) => {
          this.errorMessage.set('No se pudo cargar la información del perfil.');
          this.loading.set(false);
          this.cached$ = null;
          return throwError(() => err);
        }),
        shareReplay(1),
      );
    }
    return this.cached$;
  }

  refresh(limit = 8): Observable<SidebarData> {
    this.cached$ = null;
    return this.ensureLoaded(limit);
  }

  clear(): void {
    this.cached$ = null;
    this.profile.set(null);
    this.suggestions.set([]);
    this.errorMessage.set('');
    this.loading.set(false);
  }
}
