import { Component, OnInit, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { FeedSidebarContextService } from '../../../../core/services/feed-sidebar-context.service';
import { ProfileApiService } from '../../../../core/services/profile-api.service';
import { Perfil } from '../../../../core/models/profile.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';
import { SidebarProfileCard } from '../sidebar-profile-card/sidebar-profile-card';
import { SidebarProfile } from '../../../../core/models/user-sidebar.model';

@Component({
  selector: 'app-feed-right-sidebar',
  imports: [RouterLink, UserAvatarComponent, SidebarProfileCard],
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
}
