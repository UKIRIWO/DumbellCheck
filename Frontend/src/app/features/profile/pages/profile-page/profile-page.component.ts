import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProfileApiService } from '../../../../core/services/profile-api.service';
import { PostApiService } from '../../../../core/services/post-api.service';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { Perfil } from '../../../../core/models/profile.model';
import { PostFeedItem } from '../../../../core/models/post.model';
import { ProfileHeader } from '../../components/profile-header/profile-header';
import { ProfilePostsGrid } from '../../components/profile-posts-grid/profile-posts-grid';
import { EditProfileModal } from '../../components/edit-profile-modal/edit-profile-modal';

@Component({
  selector: 'app-profile-page',
  imports: [ProfileHeader, ProfilePostsGrid, EditProfileModal],
  templateUrl: './profile-page.component.html',
})
export class ProfilePageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly profileApi = inject(ProfileApiService);
  private readonly postApi = inject(PostApiService);
  private readonly currentUser = inject(CurrentUserService);

  readonly perfil = signal<Perfil | null>(null);
  readonly posts = signal<PostFeedItem[]>([]);
  readonly loadingPerfil = signal(true);
  readonly loadingPosts = signal(false);
  readonly errorPerfil = signal('');
  readonly hasMore = signal(false);
  readonly editOpen = signal(false);

  private cursor: number | undefined = undefined;
  private username = '';

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const u = params.get('username');
      if (!u) {
        this.router.navigateByUrl('/app/feed');
        return;
      }
      this.username = u;
      this.loadPerfil();
      this.loadPosts(true);
    });
  }

  private loadPerfil(): void {
    this.loadingPerfil.set(true);
    this.errorPerfil.set('');
    this.profileApi.getPerfil(this.username).subscribe({
      next: (p) => {
        this.perfil.set(p);
        this.loadingPerfil.set(false);
      },
      error: () => {
        this.loadingPerfil.set(false);
        this.errorPerfil.set('No se pudo cargar el perfil.');
      },
    });
  }

  loadPosts(reset = false): void {
    if (this.loadingPosts()) return;
    if (reset) {
      this.cursor = undefined;
      this.posts.set([]);
      this.hasMore.set(false);
    }
    this.loadingPosts.set(true);
    this.postApi.getPostsByUsername(this.username, this.cursor, 18).subscribe({
      next: (page) => {
        this.posts.update((prev) => [...prev, ...page.content]);
        this.hasMore.set(page.hasMore);
        this.cursor = page.nextCursor ?? undefined;
        this.loadingPosts.set(false);
      },
      error: () => {
        this.loadingPosts.set(false);
      },
    });
  }

  onEditClosed(updated: Perfil): void {
    this.editOpen.set(false);
    this.perfil.set(updated);
    if (updated.esPropio) {
      this.currentUser.refresh().subscribe({ error: () => {} });
    }
  }
}
