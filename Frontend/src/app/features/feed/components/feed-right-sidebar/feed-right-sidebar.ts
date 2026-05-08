import { Component, OnInit, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { FeedSidebarContextService } from '../../../../core/services/feed-sidebar-context.service';
import { ProfileApiService } from '../../../../core/services/profile-api.service';
import { Perfil } from '../../../../core/models/profile.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';
import { SidebarProfileCard } from '../sidebar-profile-card/sidebar-profile-card';
import { SidebarProfile } from '../../../../core/models/user-sidebar.model';
import { FollowListModal } from '../../../profile/components/follow-list-modal/follow-list-modal';

@Component({
  selector: 'app-feed-right-sidebar',
  imports: [RouterLink, UserAvatarComponent, SidebarProfileCard, FollowListModal],
  templateUrl: './feed-right-sidebar.html',
  standalone: true,
})
export class FeedRightSidebar implements OnInit {
  private readonly currentUser = inject(CurrentUserService);
  private readonly profileApi = inject(ProfileApiService);
  readonly sidebarContext = inject(FeedSidebarContextService);

  readonly loading = this.currentUser.loading;
  readonly errorMessage = this.currentUser.errorMessage;
  readonly profile = this.currentUser.profile;
  readonly suggestions = this.currentUser.suggestions;

  readonly authorProfile = signal<Perfil | null>(null);
  readonly authorLoading = signal(false);
  readonly authorError = signal('');
  readonly modalUsername = signal<string | null>(null);
  readonly modalMode = signal<'seguidores' | 'seguidos' | null>(null);
  readonly suggestionFollowState = signal<Record<number, boolean>>({});
  readonly suggestionActionLoading = signal<Record<number, boolean>>({});

  constructor() {
    effect((onCleanup) => {
      const username = this.sidebarContext.highlightedUsername();
      if (!username) {
        untracked(() => {
          this.authorProfile.set(null);
          this.authorLoading.set(false);
          this.authorError.set('');
        });
        return;
      }

      untracked(() => {
        this.authorLoading.set(true);
        this.authorError.set('');
      });

      const sub = this.profileApi.getPerfil(username).subscribe({
        next: (p) => {
          untracked(() => {
            this.authorProfile.set(p);
            this.authorLoading.set(false);
          });
        },
        error: () => {
          untracked(() => {
            this.authorProfile.set(null);
            this.authorLoading.set(false);
            this.authorError.set('No se pudo cargar el perfil.');
          });
        },
      });

      onCleanup(() => sub.unsubscribe());
    });

    effect(() => {
      const current = this.suggestions();
      this.suggestionFollowState.update((prev) => {
        const next: Record<number, boolean> = {};
        for (const s of current) {
          next[s.id] = prev[s.id] ?? false;
        }
        return next;
      });
    });
  }

  ngOnInit(): void {
    this.currentUser.ensureLoaded(8).subscribe({ error: () => {} });
  }

  perfilToSidebar(p: Perfil): SidebarProfile {
    return {
      id: p.id,
      username: p.username,
      nombre: p.nombre,
      fotoPerfilUrl: p.fotoPerfilUrl ?? null,
      contadorSeguidores: p.contadorSeguidores,
      contadorSeguidos: p.contadorSeguidos,
      contadorPublicaciones: p.contadorPublicaciones,
    };
  }

  openConnectionsModal(username: string, mode: 'seguidores' | 'seguidos'): void {
    this.modalUsername.set(username);
    this.modalMode.set(mode);
  }

  closeConnectionsModal(): void {
    this.modalUsername.set(null);
    this.modalMode.set(null);
  }

  onModalFollowChanged(event: { userId: number; username: string; seguidoPorMi: boolean }): void {
    // Keep "Puede que conozcas..." button state in sync with modal actions.
    this.suggestionFollowState.update((prev) => ({ ...prev, [event.userId]: event.seguidoPorMi }));

    this.currentUser.refresh(8).subscribe({ error: () => {} });

    const highlighted = this.sidebarContext.highlightedUsername();
    if (!highlighted) return;

    this.profileApi.getPerfil(highlighted).subscribe({
      next: (p) => this.authorProfile.set(p),
      error: () => {},
    });
  }

  isSuggestionFollowed(userId: number): boolean {
    return this.suggestionFollowState()[userId] ?? false;
  }

  isSuggestionActionLoading(userId: number): boolean {
    return !!this.suggestionActionLoading()[userId];
  }

  toggleSuggestionFollow(userId: number, username: string): void {
    if (this.isSuggestionActionLoading(userId)) return;

    const currentlyFollowed = this.isSuggestionFollowed(userId);
    this.suggestionActionLoading.update((prev) => ({ ...prev, [userId]: true }));

    const req$ = currentlyFollowed
      ? this.profileApi.dejarDeSeguirUsuario(username)
      : this.profileApi.seguirUsuario(username);

    req$.subscribe({
      next: () => {
        this.suggestionFollowState.update((prev) => ({ ...prev, [userId]: !currentlyFollowed }));
        this.suggestionActionLoading.update((prev) => ({ ...prev, [userId]: false }));

        // Keep suggestion visible, but sync my "siguiendo" counter in the sidebar card.
        this.profile.update((me) => {
          if (!me) return me;
          const delta = currentlyFollowed ? -1 : 1;
          return {
            ...me,
            contadorSeguidos: Math.max(0, me.contadorSeguidos + delta),
          };
        });

        const highlighted = this.sidebarContext.highlightedUsername();
        if (highlighted === username) {
          this.authorProfile.update((p) =>
            p
              ? {
                  ...p,
                  contadorSeguidores: Math.max(0, p.contadorSeguidores + (currentlyFollowed ? -1 : 1)),
                  sigueAEsteUsuario: !currentlyFollowed,
                }
              : p,
          );
        }
      },
      error: () => {
        this.suggestionActionLoading.update((prev) => ({ ...prev, [userId]: false }));
      },
    });
  }
}
