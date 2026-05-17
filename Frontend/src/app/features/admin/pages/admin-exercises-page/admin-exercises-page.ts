import { Component, HostListener, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminApiService } from '../../../../core/services/admin-api.service';
import { AdminEjercicio, AdminEjercicioRequest, AdminPageResponse } from '../../../../core/models/admin.model';
import { AdminPagination } from '../../components/admin-pagination/admin-pagination';
import { AdminConfirmModal } from '../../components/admin-confirm-modal/admin-confirm-modal';

interface EditingEjercicio {
  id: number;
  nombre: string;
  descripcion: string;
  imagenUrl: string;
}

@Component({
  selector: 'app-admin-exercises-page',
  imports: [DatePipe, FormsModule, AdminPagination, AdminConfirmModal],
  templateUrl: './admin-exercises-page.html',
})
export class AdminExercisesPage implements OnInit {
  private readonly adminApi = inject(AdminApiService);

  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly page = signal<AdminPageResponse<AdminEjercicio> | null>(null);
  readonly currentPage = signal(0);
  readonly sortField = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc' | null>(null);

  readonly pendingDelete = signal<AdminEjercicio | null>(null);
  readonly deleting = signal(false);
  readonly menuOpenItem = signal<AdminEjercicio | null>(null);
  readonly menuPos = signal<{ top: number; left: number }>({ top: 0, left: 0 });

  readonly editingRow = signal<EditingEjercicio | null>(null);
  readonly saving = signal(false);

  readonly showCreateForm = signal(false);
  readonly creating = signal(false);
  readonly createForm = signal<AdminEjercicioRequest>({ nombre: '', descripcion: undefined, imagenUrl: undefined });

  ngOnInit(): void { this.load(); }

  load(page = 0): void {
    this.currentPage.set(page);
    this.loading.set(true);
    this.errorMessage.set('');
    this.adminApi.getEjercicios(page, 20, this.sortField(), this.sortDir()).subscribe({
      next: (data) => { this.page.set(data); this.loading.set(false); },
      error: () => { this.errorMessage.set('Error cargando ejercicios.'); this.loading.set(false); },
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

  openMenu(event: MouseEvent, item: AdminEjercicio): void {
    event.stopPropagation();
    const btn = event.currentTarget as HTMLElement;
    const rect = btn.getBoundingClientRect();
    this.menuPos.set({ top: rect.bottom + 4, left: rect.left - 120 });
    this.menuOpenItem.set(this.menuOpenItem()?.id === item.id ? null : item);
  }

  @HostListener('document:click')
  closeMenu(): void { this.menuOpenItem.set(null); }

  startEdit(ej: AdminEjercicio): void {
    this.menuOpenItem.set(null);
    this.editingRow.set({ id: ej.id, nombre: ej.nombre, descripcion: ej.descripcion ?? '', imagenUrl: ej.imagenUrl ?? '' });
  }

  cancelEdit(): void { this.editingRow.set(null); }

  saveEdit(): void {
    const edit = this.editingRow();
    if (!edit || this.saving()) return;
    this.saving.set(true);
    const req: AdminEjercicioRequest = {
      nombre: edit.nombre,
      descripcion: edit.descripcion || undefined,
      imagenUrl: edit.imagenUrl || undefined,
    };
    this.adminApi.updateEjercicio(edit.id, req).subscribe({
      next: (updated) => {
        this.page.update((p) => p ? {
          ...p, content: p.content.map((x) => x.id === updated.id ? updated : x),
        } : p);
        this.saving.set(false);
        this.editingRow.set(null);
      },
      error: () => { this.errorMessage.set('Error guardando.'); this.saving.set(false); },
    });
  }

  updateEditField(field: keyof EditingEjercicio, value: string): void {
    const row = this.editingRow();
    if (!row) return;
    this.editingRow.set({ ...row, [field]: value });
  }

  openCreate(): void {
    this.menuOpenItem.set(null);
    this.createForm.set({ nombre: '', descripcion: undefined, imagenUrl: undefined });
    this.showCreateForm.set(true);
  }

  closeCreate(): void { this.showCreateForm.set(false); }

  submitCreate(): void {
    const f = this.createForm();
    if (!f.nombre.trim() || this.creating()) return;
    this.creating.set(true);
    this.adminApi.createEjercicio(f).subscribe({
      next: (ej) => {
        this.page.update((p) => p ? {
          ...p, content: [ej, ...p.content], totalElements: p.totalElements + 1,
        } : p);
        this.creating.set(false);
        this.showCreateForm.set(false);
      },
      error: () => { this.errorMessage.set('Error creando ejercicio.'); this.creating.set(false); },
    });
  }

  updateCreateForm(field: keyof AdminEjercicioRequest, value: string): void {
    this.createForm.update((f) => ({ ...f, [field]: value || undefined }));
  }

  askDelete(ej: AdminEjercicio): void { this.menuOpenItem.set(null); this.pendingDelete.set(ej); }
  cancelDelete(): void { this.pendingDelete.set(null); }

  confirmDelete(): void {
    const ej = this.pendingDelete();
    if (!ej || this.deleting()) return;
    this.deleting.set(true);
    this.adminApi.deleteEjercicio(ej.id).subscribe({
      next: () => {
        this.page.update((p) => p ? {
          ...p, content: p.content.filter((x) => x.id !== ej.id), totalElements: p.totalElements - 1,
        } : p);
        this.deleting.set(false);
        this.pendingDelete.set(null);
      },
      error: () => { this.errorMessage.set('Error eliminando.'); this.deleting.set(false); this.pendingDelete.set(null); },
    });
  }
}
