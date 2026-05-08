import { Component, EventEmitter, Input, OnChanges, Output, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProfileApiService } from '../../../../core/services/profile-api.service';
import { PerfilConnection } from '../../../../core/models/profile.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

type FollowListMode = 'seguidores' | 'seguidos';
type FollowChangeEvent = { userId: number; username: string; seguidoPorMi: boolean };

@Component({
  selector: 'app-follow-list-modal',
  imports: [FormsModule, RouterLink, UserAvatarComponent],
  templateUrl: './follow-list-modal.html',
})
export class FollowListModal implements OnChanges {
  private readonly profileApi = inject(ProfileApiService);

  @Input({ required: true }) username!: string;
  @Input({ required: true }) mode!: FollowListMode;
  @Output() closed = new EventEmitter<void>();
  @Output() followChanged = new EventEmitter<FollowChangeEvent>();

  readonly loading = signal(false);
  readonly error = signal('');
  readonly search = signal('');
  readonly users = signal<PerfilConnection[]>([]);
  readonly actionLoadingById = signal<Record<number, boolean>>({});

  readonly title = computed(() => (this.mode === 'seguidores' ? 'Seguidores' : 'Siguiendo'));

  readonly filteredUsers = computed(() => {
    const q = this.search().trim().toLowerCase();
    if (!q) return this.users();
    return this.users().filter((u) => {
      const username = u.username.toLowerCase();
      const nombre = (u.nombre ?? '').toLowerCase();
      return username.includes(q) || nombre.includes(q);
    });
  });

  ngOnChanges(): void {
    if (!this.username || !this.mode) return;
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.error.set('');
    this.actionLoadingById.set({});

    const req$ = this.mode === 'seguidores'
      ? this.profileApi.getSeguidores(this.username)
      : this.profileApi.getSeguidos(this.username);

    req$.subscribe({
      next: (data) => {
        this.users.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista.');
        this.loading.set(false);
      },
    });
  }

  isActionLoading(userId: number): boolean {
    return !!this.actionLoadingById()[userId];
  }

  toggleFollow(user: PerfilConnection): void {
    if (user.esPropio || this.isActionLoading(user.id)) return;

    const currentlyFollowed = user.seguidoPorMi;
    this.actionLoadingById.update((prev) => ({ ...prev, [user.id]: true }));
    const req$ = currentlyFollowed
      ? this.profileApi.dejarDeSeguirUsuario(user.username)
      : this.profileApi.seguirUsuario(user.username);

    req$.subscribe({
      next: () => {
        const newState = !currentlyFollowed;
        this.users.update((prev) => {
          const updated = prev.map((u) =>
            u.id === user.id ? { ...u, seguidoPorMi: newState } : u,
          );
          return [...updated].sort((a, b) => {
            if (a.seguidoPorMi !== b.seguidoPorMi) {
              return a.seguidoPorMi ? -1 : 1;
            }
            return a.username.localeCompare(b.username, 'es', { sensitivity: 'base' });
          });
        });
        this.followChanged.emit({
          userId: user.id,
          username: user.username,
          seguidoPorMi: newState,
        });
        this.actionLoadingById.update((prev) => ({ ...prev, [user.id]: false }));
      },
      error: () => {
        this.actionLoadingById.update((prev) => ({ ...prev, [user.id]: false }));
      },
    });
  }
}
