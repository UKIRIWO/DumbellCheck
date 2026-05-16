import { Component, OnInit, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { RoutineApiService } from '../../../../core/services/routine-api.service';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { RoutineListItem } from '../../../../core/models/routine.model';

@Component({
  selector: 'app-routines-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './routines-page.component.html',
  styleUrl: './routines-page.component.scss',
})
export class RoutinesPageComponent implements OnInit {
  private readonly routineApi = inject(RoutineApiService);
  readonly currentUser = inject(CurrentUserService);
  private readonly router = inject(Router);

  readonly routines = signal<RoutineListItem[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');
  readonly activeMenuId = signal<string | null>(null);
  readonly pendingDelete = signal<RoutineListItem | null>(null);


  readonly draggedIndex = signal<number | null>(null);
  readonly dragOverIndex = signal<number | null>(null);

  ngOnInit(): void {
    this.loadMyRutinas();
  }

  private loadMyRutinas(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.routineApi.getMyRutinas().subscribe({
      next: (data) => {
        this.routines.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudieron cargar las rutinas.');
        this.loading.set(false);
      },
    });
  }

  exercisesSummary(routine: RoutineListItem): string {
    const names = routine.nombresEjercicios;
    if (names.length === 0) return '';
    const shown = names.slice(0, 4).join(', ');
    return names.length > 4 ? `${shown}…` : shown;
  }

  openDetail(routine: RoutineListItem): void {
    this.router.navigate(['/app/routines', routine.publicId]);
  }

  openMenu(event: Event, routine: RoutineListItem): void {
    event.stopPropagation();
    this.activeMenuId.set(this.activeMenuId() === routine.publicId ? null : routine.publicId);
  }

  editRoutine(event: Event, routine: RoutineListItem): void {
    event.stopPropagation();
    this.activeMenuId.set(null);
    this.router.navigate(['/app/routines', routine.publicId, 'editar']);
  }

  createPostFromRoutine(event: Event, routine: RoutineListItem): void {
    event.stopPropagation();
    this.activeMenuId.set(null);
    this.router.navigate(['/app/feed/crear'], {
      state: { preloadRutinaPublicId: routine.publicId },
    });
  }

  askDelete(event: Event, routine: RoutineListItem): void {
    event.stopPropagation();
    this.activeMenuId.set(null);
    this.pendingDelete.set(routine);
  }

  cancelDelete(): void {
    this.pendingDelete.set(null);
  }

  confirmDelete(): void {
    const routine = this.pendingDelete();
    if (!routine) return;
    this.pendingDelete.set(null);
    this.routineApi.deleteRutina(routine.publicId).subscribe({
      next: () => this.routines.update((prev) => prev.filter((r) => r.publicId !== routine.publicId)),
      error: () => this.errorMessage.set('No se pudo eliminar la rutina.'),
    });
  }



  onDragStart(event: DragEvent, index: number): void {
    this.draggedIndex.set(index);
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
    }
  }

  onDragOver(event: DragEvent, index: number): void {
    event.preventDefault();
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
    if (this.draggedIndex() !== index) {
      this.dragOverIndex.set(index);
    }
  }

  onDragLeave(): void {
    this.dragOverIndex.set(null);
  }

  onDrop(event: DragEvent, dropIndex: number): void {
    event.preventDefault();
    const dragIndex = this.draggedIndex();
    if (dragIndex === null || dragIndex === dropIndex) {
      this.draggedIndex.set(null);
      this.dragOverIndex.set(null);
      return;
    }
    this.routines.update((prev) => {
      const list = [...prev];
      const [removed] = list.splice(dragIndex, 1);
      list.splice(dropIndex, 0, removed);
      return list;
    });
    this.draggedIndex.set(null);
    this.dragOverIndex.set(null);
  }

  onDragEnd(): void {
    this.draggedIndex.set(null);
    this.dragOverIndex.set(null);
  }
}
