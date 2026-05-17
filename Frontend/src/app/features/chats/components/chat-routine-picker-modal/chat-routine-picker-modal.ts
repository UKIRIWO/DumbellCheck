import { Component, EventEmitter, OnInit, Output, inject, signal } from '@angular/core';
import { RoutineApiService } from '../../../../core/services/routine-api.service';
import { RoutineListItem } from '../../../../core/models/routine.model';

@Component({
  selector: 'app-chat-routine-picker-modal',
  templateUrl: './chat-routine-picker-modal.html',
})
export class ChatRoutinePickerModal implements OnInit {
  private readonly routineApi = inject(RoutineApiService);

  @Output() closed = new EventEmitter<void>();
  @Output() routineSelected = new EventEmitter<number>();

  readonly routines = signal<RoutineListItem[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal('');

  ngOnInit(): void {
    this.routineApi.getMyRutinas().subscribe({
      next: (data) => {
        this.routines.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudieron cargar tus rutinas.');
        this.loading.set(false);
      },
    });
  }

  select(routine: RoutineListItem): void {
    this.routineSelected.emit(routine.id);
  }
}
