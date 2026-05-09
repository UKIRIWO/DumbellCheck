import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { debounceTime, Subject, switchMap } from 'rxjs';
import { ExerciseApiService } from '../../../../core/services/exercise-api.service';
import { Ejercicio, GrupoMuscular } from '../../../../core/models/exercise.model';

@Component({
  selector: 'app-exercise-selector',
  imports: [FormsModule],
  templateUrl: './exercise-selector.component.html',
})
export class ExerciseSelectorComponent implements OnInit {
  /** Filtro de grupo recordado mientras se crea la misma publicación (lo establece el padre). */
  @Input() savedGrupoMuscularId: number | undefined = undefined;

  /** Ejercicios ya añadidos al entreno; no se listan de nuevo (misma referencia que en el padre). */
  @Input() workoutExercises: ReadonlyArray<{ ejercicioId: number }> = [];

  @Output() ejercicioSelected = new EventEmitter<Ejercicio>();
  @Output() grupoMuscularFilterChange = new EventEmitter<number | undefined>();
  @Output() closed = new EventEmitter<void>();

  private readonly exerciseApi = inject(ExerciseApiService);

  grupos = signal<GrupoMuscular[]>([]);
  ejercicios = signal<Ejercicio[]>([]);
  loading = signal(false);

  searchQuery = '';
  selectedGrupoId: number | undefined = undefined;

  private readonly triggerSearch$ = new Subject<void>();

  ngOnInit(): void {
    this.selectedGrupoId = this.savedGrupoMuscularId;

    this.exerciseApi.getGruposMusculares().subscribe((g) => this.grupos.set(g));

    this.triggerSearch$
      .pipe(
        debounceTime(300),
        switchMap(() => {
          this.loading.set(true);
          return this.exerciseApi.getEjercicios(
            this.searchQuery || undefined,
            this.selectedGrupoId,
          );
        }),
      )
      .subscribe({
        next: (result) => {
          const yaEnEntreno = new Set(
            (this.workoutExercises ?? []).map((e) => e.ejercicioId),
          );
          this.ejercicios.set(result.filter((ej) => !yaEnEntreno.has(ej.id)));
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });

    this.triggerSearch$.next();
  }

  onSearchChange(): void {
    this.triggerSearch$.next();
  }

  onGrupoChange(): void {
    this.grupoMuscularFilterChange.emit(this.selectedGrupoId);
    this.triggerSearch$.next();
  }

  select(ej: Ejercicio): void {
    this.ejercicioSelected.emit(ej);
  }

  close(): void {
    this.closed.emit();
  }
}

