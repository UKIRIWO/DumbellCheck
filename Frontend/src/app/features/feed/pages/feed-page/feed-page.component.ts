import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { PostApiService } from '../../../../core/services/post-api.service';
import { PageResponse, PostFeedItem } from '../../../../core/models/post.model';
import { PostCardComponent } from '../../components/post-card/post-card.component';

type FeedTab = 'descubrir' | 'publico' | 'amigos';

@Component({
  selector: 'app-feed-page',
  imports: [RouterLink, PostCardComponent],
  templateUrl: './feed-page.component.html',
})
export class FeedPageComponent implements OnInit {
  private readonly postApi = inject(PostApiService);

  activeTab = signal<FeedTab>('publico');
  posts = signal<PostFeedItem[]>([]);
  loading = signal(false);
  errorMessage = signal('');
  currentPage = signal(0);
  hasMore = signal(true);

  readonly emptyTitle = computed(() => {
    switch (this.activeTab()) {
      case 'amigos':
        return 'Aún no hay publicaciones de amigos';
      case 'descubrir':
        return 'No hay nada nuevo por descubrir';
      default:
        return 'Aún no hay publicaciones';
    }
  });

  readonly emptySubtitle = computed(() => {
    switch (this.activeTab()) {
      case 'amigos':
        return 'Sigue a más usuarios para ver sus entrenamientos aquí.';
      case 'descubrir':
        return 'Vuelve más tarde para encontrar nuevos perfiles.';
      default:
        return '¡Sé el primero en compartir tu entrenamiento!';
    }
  });

  ngOnInit(): void {
    this.loadFeed(true);
  }

  setTab(tab: FeedTab): void {
    if (this.activeTab() === tab) return;
    this.activeTab.set(tab);
    this.loadFeed(true);
  }

  loadFeed(reset = false): void {
    if (reset) {
      this.currentPage.set(0);
      this.posts.set([]);
      this.hasMore.set(true);
    }
    if (this.loading() || !this.hasMore()) return;

    this.loading.set(true);
    this.errorMessage.set('');

    const tab = this.activeTab();
    this.fetchByTab(tab, this.currentPage(), 20).subscribe({
      next: (page) => {
        this.posts.update((prev) => [...prev, ...page.content]);
        this.hasMore.set(!page.last);
        this.currentPage.update((p) => p + 1);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('No se pudieron cargar las publicaciones.');
      },
    });
  }

  private fetchByTab(tab: FeedTab, page: number, size: number): Observable<PageResponse<PostFeedItem>> {
    switch (tab) {
      case 'amigos':
        return this.postApi.getFeedAmigos(page, size);
      case 'descubrir':
        return this.postApi.getFeedDescubrir(page, size);
      default:
        return this.postApi.getFeedPublico(page, size);
    }
  }
}
