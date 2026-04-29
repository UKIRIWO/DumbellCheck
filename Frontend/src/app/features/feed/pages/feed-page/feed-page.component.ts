import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PostApiService } from '../../../../core/services/post-api.service';
import { PostFeedItem } from '../../../../core/models/post.model';
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

  ngOnInit(): void {
    this.loadFeed(true);
  }

  setTab(tab: FeedTab): void {
    this.activeTab.set(tab);
    if (tab === 'publico') {
      this.loadFeed(true);
    }
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

    this.postApi.getFeedPublico(this.currentPage(), 20).subscribe({
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
}
