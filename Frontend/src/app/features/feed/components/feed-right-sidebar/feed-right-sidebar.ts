import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { UserApiService } from '../../../../core/services/user-api.service';
import { SidebarProfile, SidebarSuggestion } from '../../../../core/models/user-sidebar.model';

@Component({
  selector: 'app-feed-right-sidebar',
  imports: [RouterLink],
  templateUrl: './feed-right-sidebar.html',
  standalone: true,
})
export class FeedRightSidebar implements OnInit {
  private readonly userApi = inject(UserApiService);

  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly profile = signal<SidebarProfile | null>(null);
  readonly suggestions = signal<SidebarSuggestion[]>([]);
  readonly profileInitial = computed(() => this.profile()?.username?.charAt(0).toUpperCase() ?? 'U');

  ngOnInit(): void {
    this.userApi.getSidebarData(8).subscribe({
      next: (data) => {
        this.profile.set(data.perfil);
        this.suggestions.set(data.sugerencias);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('No se pudo cargar la información lateral.');
      },
    });
  }
}
