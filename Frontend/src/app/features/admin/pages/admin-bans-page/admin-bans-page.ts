import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminApiService } from '../../../../core/services/admin-api.service';
import { AdminBan, AdminBanUpdateRequest, AdminPageResponse } from '../../../../core/models/admin.model';
import { AdminPagination } from '../../components/admin-pagination/admin-pagination';
import { AdminConfirmModal } from '../../components/admin-confirm-modal/admin-confirm-modal';

interface EditingBan {
  id: number;
  motivoBaneo: string;
  baneadoHasta: string;
  baneadoPermanentemente: boolean;
}

@Component({
  selector: 'app-admin-bans-page',
  imports: [DatePipe, FormsModule, AdminPagination, AdminConfirmModal],
  templateUrl: './admin-bans-page.html',
})
export class AdminBansPage implements OnInit {
  private readonly adminApi = inject(AdminApiService);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly page = signal<AdminPageResponse<AdminBan> | null>(null);
  readonly currentPage = signal(0);
  readonly sortField = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc' | null>(null);

  readonly pendingDelete = signal<AdminBan | null>(null);
  readonly deleting = signal(false);
  readonly menuOpenItem = signal<AdminBan | null>(null);
  readonly menuPos = signal<{ top: number; left: number }>({ top: 0, left: 0 });

  readonly editingRow = signal<EditingBan | null>(null);
  readonly saving = signal(false);

  private menuBtnRect: DOMRect | null = null;

  readonly floatPanel = signal<{ top: number; left: number } | null>(null);

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.currentPage.set(page);
    this.loading.set(true);
    this.errorMessage.set('');
    const isFrontendSort = this.sortField() === 'activo';
    this.adminApi
      .getBans(page, 20, isFrontendSort ? null : this.sortField(), isFrontendSort ? null : this.sortDir())
      .subscribe({
        next: (data) => {
          if (isFrontendSort) {
            const dir = this.sortDir() === 'asc' ? 1 : -1;
            const sorted = [...data.content].sort(
              (a, b) => (this.isBanActive(a) ? 1 : 0) - (this.isBanActive(b) ? 1 : 0),
            );
            this.page.set({ ...data, content: dir === 1 ? sorted : sorted.reverse() });
          } else {
            this.page.set(data);
          }
          this.loading.set(false);
        },
        error: () => { this.errorMessage.set('Error cargando baneos.'); this.loading.set(false); },
      });
  }

  isBanActive(ban: AdminBan): boolean {
    return ban.baneadoPermanentemente || (!!ban.baneadoHasta && new Date(ban.baneadoHasta) > new Date());
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

  openMenu(event: MouseEvent, item: AdminBan): void {
    event.stopPropagation();
    const btn = event.currentTarget as HTMLElement;
    const rect = btn.getBoundingClientRect();
    this.menuBtnRect = rect;
    this.menuPos.set({ top: rect.bottom + 4, left: rect.left - 120 });
    this.menuOpenItem.set(this.menuOpenItem()?.id === item.id ? null : item);
  }

  @HostListener('document:click')
  closeMenu(): void { this.menuOpenItem.set(null); }

  startEdit(ban: AdminBan): void {
    this.menuOpenItem.set(null);
    if (this.menuBtnRect) {
      const left = Math.max(4, this.menuBtnRect.left - 76);
      this.floatPanel.set({ top: this.menuBtnRect.top - 4, left });
    }
    this.editingRow.set({
      id: ban.id,
      motivoBaneo: ban.motivoBaneo ?? '',
      baneadoHasta: ban.baneadoHasta ? this.toLocalDatetime(ban.baneadoHasta) : '',
      baneadoPermanentemente: ban.baneadoPermanentemente,
    });
  }

  cancelEdit(): void {
    this.editingRow.set(null);
    this.floatPanel.set(null);
  }

  saveEdit(): void {
    const edit = this.editingRow();
    if (!edit || this.saving()) return;
    this.saving.set(true);
    const req: AdminBanUpdateRequest = {
      motivoBaneo: edit.motivoBaneo || undefined,
      baneadoHasta: edit.baneadoPermanentemente ? undefined
        : (edit.baneadoHasta ? new Date(edit.baneadoHasta).toISOString() : undefined),
      baneadoPermanentemente: edit.baneadoPermanentemente,
    };
    this.adminApi.updateBan(edit.id, req).subscribe({
      next: (updated) => {
        this.page.update((p) => p ? {
          ...p, content: p.content.map((x) => x.id === updated.id ? updated : x),
        } : p);
        this.saving.set(false);
        this.editingRow.set(null);
        this.floatPanel.set(null);
      },
      error: () => { this.errorMessage.set('Error guardando baneo.'); this.saving.set(false); },
    });
  }

  updateEditField(field: keyof EditingBan, value: unknown): void {
    const row = this.editingRow();
    if (!row) return;
    if (field === 'baneadoPermanentemente' && value === true) {
      this.editingRow.set({ ...row, baneadoPermanentemente: true, baneadoHasta: '' });
      return;
    }
    this.editingRow.set({ ...row, [field]: value });
  }

  desbanear(ban: AdminBan): void {
    this.menuOpenItem.set(null);
    const req: AdminBanUpdateRequest = {
      motivoBaneo: ban.motivoBaneo,
      baneadoPermanentemente: false,
      baneadoHasta: new Date().toISOString(),
    };
    this.adminApi.updateBan(ban.id, req).subscribe({
      next: (updated) => {
        this.page.update((p) => p ? {
          ...p, content: p.content.map((x) => x.id === updated.id ? updated : x),
        } : p);
      },
      error: () => this.errorMessage.set('Error al desbanear.'),
    });
  }

  askDelete(ban: AdminBan): void { this.menuOpenItem.set(null); this.pendingDelete.set(ban); }
  cancelDelete(): void { this.pendingDelete.set(null); }

  confirmDelete(): void {
    const ban = this.pendingDelete();
    if (!ban || this.deleting()) return;
    this.deleting.set(true);
    this.adminApi.deleteBan(ban.id).subscribe({
      next: () => {
        this.page.update((p) => p ? {
          ...p, content: p.content.filter((x) => x.id !== ban.id), totalElements: p.totalElements - 1,
        } : p);
        this.deleting.set(false);
        this.pendingDelete.set(null);
      },
      error: () => { this.errorMessage.set('Error eliminando.'); this.deleting.set(false); this.pendingDelete.set(null); },
    });
  }

  private toLocalDatetime(iso: string): string {
    const d = new Date(iso);
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
}
