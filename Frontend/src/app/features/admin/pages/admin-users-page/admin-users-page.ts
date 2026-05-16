import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminApiService } from '../../../../core/services/admin-api.service';
import {
  AdminUser,
  AdminUserUpdateRequest,
  AdminBanCreateRequest,
  RolUsuario,
  AdminPageResponse,
} from '../../../../core/models/admin.model';
import { AdminPagination } from '../../components/admin-pagination/admin-pagination';
import { AdminConfirmModal } from '../../components/admin-confirm-modal/admin-confirm-modal';

@Component({
  selector: 'app-admin-users-page',
  imports: [DatePipe, FormsModule, AdminPagination, AdminConfirmModal],
  templateUrl: './admin-users-page.html',
})
export class AdminUsersPage implements OnInit {
  private readonly adminApi = inject(AdminApiService);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly page = signal<AdminPageResponse<AdminUser> | null>(null);
  readonly currentPage = signal(0);
  readonly searchQuery = signal('');

  readonly sortField = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc' | null>(null);

  readonly rolOptions: RolUsuario[] = ['MEMBER', 'ADMIN', 'SUPPORT'];

  readonly pendingDelete = signal<AdminUser | null>(null);
  readonly deleting = signal(false);

  readonly menuOpenItem = signal<AdminUser | null>(null);
  readonly menuPos = signal<{ top: number; left: number }>({ top: 0, left: 0 });

  readonly viewUser = signal<AdminUser | null>(null);

  readonly banTarget = signal<AdminUser | null>(null);
  readonly creatingBan = signal(false);
  readonly banForm = signal<AdminBanCreateRequest>({
    usuarioId: 0,
    motivoBaneo: '',
    baneadoHasta: undefined,
    baneadoPermanentemente: false,
  });

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.currentPage.set(page);
    this.loading.set(true);
    this.errorMessage.set('');
    this.adminApi
      .getUsers(page, 20, this.searchQuery() || undefined, this.sortField(), this.sortDir())
      .subscribe({
        next: (data) => { this.page.set(data); this.loading.set(false); },
        error: () => { this.errorMessage.set('Error cargando usuarios.'); this.loading.set(false); },
      });
  }

  search(): void { this.load(0); }

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

  openMenu(event: MouseEvent, item: AdminUser): void {
    event.stopPropagation();
    const btn = event.currentTarget as HTMLElement;
    const rect = btn.getBoundingClientRect();
    this.menuPos.set({ top: rect.bottom + 4, left: rect.left - 120 });
    this.menuOpenItem.set(this.menuOpenItem()?.id === item.id ? null : item);
  }

  @HostListener('document:click')
  closeMenu(): void { this.menuOpenItem.set(null); }

  hasActiveBan(user: AdminUser): boolean {
    return user.baneos.some(
      (b) => b.baneadoPermanentemente || (!!b.baneadoHasta && new Date(b.baneadoHasta) > new Date()),
    );
  }

  changeRol(user: AdminUser, rol: RolUsuario): void {
    const req: AdminUserUpdateRequest = { rol };
    this.adminApi.updateUser(user.id, req).subscribe({
      next: (updated) => {
        this.page.update((p) => p ? {
          ...p, content: p.content.map((u) => u.id === updated.id ? updated : u),
        } : p);
      },
      error: () => this.errorMessage.set('Error actualizando rol.'),
    });
  }

  openView(user: AdminUser): void {
    this.menuOpenItem.set(null);
    this.viewUser.set(user);
  }

  openBan(user: AdminUser): void {
    this.menuOpenItem.set(null);
    this.banForm.set({
      usuarioId: user.id,
      motivoBaneo: '',
      baneadoHasta: undefined,
      baneadoPermanentemente: false,
    });
    this.banTarget.set(user);
  }

  closeBan(): void { this.banTarget.set(null); }

  submitBan(): void {
    const f = this.banForm();
    if (!f.usuarioId || this.creatingBan()) return;
    this.creatingBan.set(true);
    this.adminApi.createBan(f).subscribe({
      next: (ban) => {
        this.page.update((p) => p ? {
          ...p,
          content: p.content.map((u) =>
            u.id === f.usuarioId ? { ...u, baneos: [...u.baneos, ban] } : u,
          ),
        } : p);
        this.creatingBan.set(false);
        this.banTarget.set(null);
      },
      error: () => { this.errorMessage.set('Error creando baneo.'); this.creatingBan.set(false); },
    });
  }

  updateBanForm(field: keyof AdminBanCreateRequest, value: unknown): void {
    if (field === 'baneadoHasta' && typeof value === 'string' && value) {
      value = new Date(value).toISOString();
    }
    this.banForm.update((f) => ({ ...f, [field]: value }));
  }

  askDelete(user: AdminUser): void { this.menuOpenItem.set(null); this.pendingDelete.set(user); }
  cancelDelete(): void { this.pendingDelete.set(null); }

  confirmDelete(): void {
    const user = this.pendingDelete();
    if (!user || this.deleting()) return;
    this.deleting.set(true);
    this.adminApi.deleteUser(user.id).subscribe({
      next: () => {
        this.page.update((p) => p ? {
          ...p, content: p.content.filter((u) => u.id !== user.id), totalElements: p.totalElements - 1,
        } : p);
        this.deleting.set(false);
        this.pendingDelete.set(null);
      },
      error: () => { this.errorMessage.set('Error eliminando.'); this.deleting.set(false); this.pendingDelete.set(null); },
    });
  }
}
