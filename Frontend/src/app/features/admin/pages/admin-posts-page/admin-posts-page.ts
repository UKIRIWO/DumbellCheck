import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AdminApiService } from '../../../../core/services/admin-api.service';
import { AdminPost, AdminPageResponse } from '../../../../core/models/admin.model';
import { AdminPagination } from '../../components/admin-pagination/admin-pagination';
import { AdminConfirmModal } from '../../components/admin-confirm-modal/admin-confirm-modal';

@Component({
  selector: 'app-admin-posts-page',
  imports: [DatePipe, AdminPagination, AdminConfirmModal],
  templateUrl: './admin-posts-page.html',
})
export class AdminPostsPage implements OnInit {
  private readonly adminApi = inject(AdminApiService);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly page = signal<AdminPageResponse<AdminPost> | null>(null);
  readonly currentPage = signal(0);
  readonly sortField = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc' | null>(null);
  readonly pendingDelete = signal<AdminPost | null>(null);
  readonly deleting = signal(false);
  readonly menuOpenItem = signal<AdminPost | null>(null);
  readonly menuPos = signal<{ top: number; left: number }>({ top: 0, left: 0 });
  readonly mediaModal = signal<{ url: string; type: 'image' | 'video' } | null>(null);

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.currentPage.set(page);
    this.loading.set(true);
    this.errorMessage.set('');
    this.adminApi.getPosts(page, 20, this.sortField(), this.sortDir()).subscribe({
      next: (data) => { this.page.set(data); this.loading.set(false); },
      error: () => { this.errorMessage.set('Error cargando publicaciones.'); this.loading.set(false); },
    });
  }

  toggleSort(field: string): void {
    if (this.sortField() !== field) {
      this.sortField.set(field); this.sortDir.set('asc');
    } else if (this.sortDir() === 'asc') {
      this.sortDir.set('desc');
    } else {
      this.sortField.set(null); this.sortDir.set(null);
    }
    this.load(0);
  }

  sortIcon(field: string): string {
    if (this.sortField() !== field || !this.sortDir()) return '';
    return this.sortDir() === 'asc' ? '▴' : '▾';
  }

  openMenu(event: MouseEvent, item: AdminPost): void {
    event.stopPropagation();
    const btn = event.currentTarget as HTMLElement;
    const rect = btn.getBoundingClientRect();
    this.menuPos.set({ top: rect.bottom + 4, left: rect.left - 120 });
    this.menuOpenItem.set(this.menuOpenItem()?.id === item.id ? null : item);
  }

  @HostListener('document:click')
  closeMenu(): void { this.menuOpenItem.set(null); }

  toggleActiva(post: AdminPost): void {
    this.menuOpenItem.set(null);
    this.adminApi.togglePostActiva(post.id).subscribe({
      next: (updated) => this.page.update((p) => p ? {
        ...p, content: p.content.map((x) => x.id === updated.id ? updated : x),
      } : p),
      error: () => this.errorMessage.set('Error actualizando estado.'),
    });
  }

  openMedia(post: AdminPost): void {
    if (!post.multimediaUrl) return;
    const url = post.multimediaUrl.toLowerCase();
    const type = url.match(/\.(mp4|webm|ogg|mov)/) ? 'video' : 'image';
    this.mediaModal.set({ url: post.multimediaUrl, type });
    this.menuOpenItem.set(null);
  }

  askDelete(post: AdminPost): void { this.menuOpenItem.set(null); this.pendingDelete.set(post); }
  cancelDelete(): void { this.pendingDelete.set(null); }

  confirmDelete(): void {
    const post = this.pendingDelete();
    if (!post || this.deleting()) return;
    this.deleting.set(true);
    this.adminApi.deletePost(post.id).subscribe({
      next: () => {
        this.page.update((p) => p ? {
          ...p, content: p.content.filter((x) => x.id !== post.id), totalElements: p.totalElements - 1,
        } : p);
        this.deleting.set(false);
        this.pendingDelete.set(null);
      },
      error: () => { this.errorMessage.set('Error eliminando.'); this.deleting.set(false); this.pendingDelete.set(null); },
    });
  }
}
