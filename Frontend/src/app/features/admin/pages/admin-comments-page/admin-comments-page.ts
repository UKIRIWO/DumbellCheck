import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AdminApiService } from '../../../../core/services/admin-api.service';
import { AdminComment, AdminPageResponse } from '../../../../core/models/admin.model';
import { AdminPagination } from '../../components/admin-pagination/admin-pagination';
import { AdminConfirmModal } from '../../components/admin-confirm-modal/admin-confirm-modal';

@Component({
  selector: 'app-admin-comments-page',
  imports: [DatePipe, AdminPagination, AdminConfirmModal],
  templateUrl: './admin-comments-page.html',
})
export class AdminCommentsPage implements OnInit {
  private readonly adminApi = inject(AdminApiService);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly page = signal<AdminPageResponse<AdminComment> | null>(null);
  readonly currentPage = signal(0);
  readonly sortField = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc' | null>(null);
  readonly pendingDelete = signal<AdminComment | null>(null);
  readonly deleting = signal(false);
  readonly menuOpenItem = signal<AdminComment | null>(null);
  readonly menuPos = signal<{ top: number; left: number }>({ top: 0, left: 0 });

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.currentPage.set(page);
    this.loading.set(true);
    this.errorMessage.set('');
    this.adminApi.getComments(page, 20, this.sortField(), this.sortDir()).subscribe({
      next: (data) => { this.page.set(data); this.loading.set(false); },
      error: () => { this.errorMessage.set('Error cargando comentarios.'); this.loading.set(false); },
    });
  }

  toggleSort(field: string): void {
    if (this.sortField() !== field) { this.sortField.set(field); this.sortDir.set('asc'); }
    else if (this.sortDir() === 'asc') { this.sortDir.set('desc'); }
    else { this.sortField.set(null); this.sortDir.set(null); }
    this.load(0);
  }

  sortIcon(field: string): string {
    if (this.sortField() !== field || !this.sortDir()) return '';
    return this.sortDir() === 'asc' ? '▴' : '▾';
  }

  openMenu(event: MouseEvent, item: AdminComment): void {
    event.stopPropagation();
    const btn = event.currentTarget as HTMLElement;
    const rect = btn.getBoundingClientRect();
    this.menuPos.set({ top: rect.bottom + 4, left: rect.left - 120 });
    this.menuOpenItem.set(this.menuOpenItem()?.id === item.id ? null : item);
  }

  @HostListener('document:click')
  closeMenu(): void { this.menuOpenItem.set(null); }

  askDelete(comment: AdminComment): void { this.menuOpenItem.set(null); this.pendingDelete.set(comment); }
  cancelDelete(): void { this.pendingDelete.set(null); }

  confirmDelete(): void {
    const comment = this.pendingDelete();
    if (!comment || this.deleting()) return;
    this.deleting.set(true);
    this.adminApi.deleteComment(comment.id).subscribe({
      next: () => {
        this.page.update((p) => p ? {
          ...p, content: p.content.filter((x) => x.id !== comment.id), totalElements: p.totalElements - 1,
        } : p);
        this.deleting.set(false);
        this.pendingDelete.set(null);
      },
      error: () => { this.errorMessage.set('Error eliminando.'); this.deleting.set(false); this.pendingDelete.set(null); },
    });
  }

  truncate(text: string, max = 80): string {
    return text.length > max ? text.slice(0, max) + '…' : text;
  }
}
