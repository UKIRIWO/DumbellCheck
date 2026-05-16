import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { RoutineApiService } from '../../../../core/services/routine-api.service';
import { Ejercicio } from '../../../../core/models/exercise.model';
import { CreateRoutineRequest, Routine } from '../../../../core/models/routine.model';
import { ExerciseSelectorComponent } from '../../../feed/components/exercise-selector/exercise-selector.component';


export interface WorkingSerie {
  repeticiones: number | null;
  peso: number | null;
  descansoInput: string;
}

export interface WorkingEjercicio {
  ejercicioId: number;
  nombre: string;
  imagenUrl?: string;
  notas?: string;
  series: WorkingSerie[];
}

function parseRestInput(input: string): number | null {
  const trimmed = input?.trim();
  if (!trimmed) return null;
  const mmss = /^(\d+):([0-5]\d)$/.exec(trimmed);
  if (mmss) return parseInt(mmss[1]) * 60 + parseInt(mmss[2]);
  if (/^\d+$/.test(trimmed)) {
    const n = parseInt(trimmed);
    return n >= 0 ? n : null;
  }
  return null;
}

function isValidRestInput(input: string): boolean {
  const trimmed = input?.trim();
  if (!trimmed) return true;
  return /^(\d+):([0-5]\d)$/.test(trimmed) || /^\d+$/.test(trimmed);
}

@Component({
  selector: 'app-routine-form-page',
  standalone: true,
  imports: [FormsModule, ExerciseSelectorComponent],
  templateUrl: './routine-form-page.component.html',
})
export class RoutineFormPageComponent implements OnInit, OnDestroy {
  private readonly routineApi = inject(RoutineApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly isEdit = signal(false);
  readonly loadingEdit = signal(false);
  private editPublicId: string | null = null;

  nombre = '';
  descripcion = '';
  exercises: WorkingEjercicio[] = [];

  readonly showExerciseSelector = signal(false);
  persistedSelectorGrupoId: number | undefined = undefined;
  readonly submitting = signal(false);
  readonly errorMessage = signal('');
  readonly isValidRest = isValidRestInput;

  private sub?: Subscription;

  get canSubmit(): boolean {
    return (
      this.nombre.trim().length > 0 &&
      this.exercises.length > 0 &&
      this.exercises.every((ex) => {
        if (ex.series.length === 0) return false;
        const first = ex.series[0];
        return (
          first.repeticiones != null &&
          first.peso != null &&
          ex.series.every((s) => isValidRestInput(s.descansoInput))
        );
      })
    );
  }

  ngOnInit(): void {

    this.sub = this.route.paramMap.subscribe((params) => {
      const publicId = params.get('publicId');
      if (publicId && publicId !== this.editPublicId) {
        this.isEdit.set(true);
        this.editPublicId = publicId;
        this.loadRoutine(publicId);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  private loadRoutine(publicId: string): void {
    this.loadingEdit.set(true);
    this.routineApi.getRutina(publicId).subscribe({
      next: (routine) => {
        this.populateFromRoutine(routine);
        this.loadingEdit.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudo cargar la rutina.');
        this.loadingEdit.set(false);
      },
    });
  }

  private populateFromRoutine(routine: Routine): void {
    this.nombre = routine.nombre;
    this.descripcion = routine.descripcion ?? '';
    this.exercises = routine.ejercicios.map((ej) => ({
      ejercicioId: ej.ejercicioId,
      nombre: ej.nombre,
      imagenUrl: ej.imagenUrl ?? undefined,
      notas: ej.notas ?? undefined,
      series: ej.series.map((s) => ({
        repeticiones: s.repeticiones,
        peso: s.peso,
        descansoInput: s.descansoSegundos != null ? String(s.descansoSegundos) : '',
      })),
    }));
  }

  onEjercicioSelected(ej: Ejercicio): void {
    if (!this.exercises.some((e) => e.ejercicioId === ej.id)) {
      this.exercises.push({
        ejercicioId: ej.id,
        nombre: ej.nombre,
        imagenUrl: ej.imagenUrl,
        series: [this.newSerie()],
      });
    }
    this.showExerciseSelector.set(false);
  }

  removeEjercicio(index: number): void {
    this.exercises.splice(index, 1);
  }

  addSerie(ej: WorkingEjercicio): void {
    ej.series.push(this.newSerie());
  }

  removeSerie(ej: WorkingEjercicio, idx: number): void {
    ej.series.splice(idx, 1);
  }

  private newSerie(): WorkingSerie {
    return { repeticiones: null, peso: null, descansoInput: '' };
  }

  getPesoPlaceholder(ejercicio: WorkingEjercicio, serieIndex: number): string {
    if (serieIndex === 0) return '0';
    return String(this.findPreviousValue<number>(ejercicio, serieIndex, (s) => s.peso) ?? '0');
  }

  getRepsPlaceholder(ejercicio: WorkingEjercicio, serieIndex: number): string {
    if (serieIndex === 0) return '0';
    return String(this.findPreviousValue<number>(ejercicio, serieIndex, (s) => s.repeticiones) ?? '0');
  }

  getDescansoPlaceholder(ejercicio: WorkingEjercicio, serieIndex: number): string {
    if (serieIndex === 0) return 'seg / mm:ss';
    return (
      this.findPreviousValue<string>(ejercicio, serieIndex, (s) => {
        const t = s.descansoInput.trim();
        return t ? t : null;
      }) ?? 'seg / mm:ss'
    );
  }

  private findPreviousValue<T>(
    ejercicio: WorkingEjercicio,
    serieIndex: number,
    selector: (serie: WorkingSerie) => T | null,
  ): T | null {
    for (let i = serieIndex - 1; i >= 0; i--) {
      const value = selector(ejercicio.series[i]);
      if (value != null) return value;
    }
    return null;
  }

  private buildSeriesPayload(ex: WorkingEjercicio): CreateRoutineRequest['ejercicios'][number]['series'] {
    const result: CreateRoutineRequest['ejercicios'][number]['series'] = [];
    let lastReps: number | null = null;
    let lastPeso: number | null = null;
    let lastDescanso: number | undefined = undefined;

    ex.series.forEach((s, idx) => {
      const repeticiones = s.repeticiones ?? lastReps;
      const peso = s.peso ?? lastPeso;
      const descansoParsed = parseRestInput(s.descansoInput);
      const descansoSegundos = descansoParsed ?? lastDescanso;

      if (repeticiones == null || peso == null) throw new Error('Serie inválida');

      result.push({ numeroSerie: idx + 1, repeticiones, peso, descansoSegundos });
      lastReps = repeticiones;
      lastPeso = peso;
      lastDescanso = descansoSegundos;
    });

    return result;
  }

  submit(): void {
    if (!this.canSubmit || this.submitting()) return;
    this.errorMessage.set('');
    this.submitting.set(true);

    const payload: CreateRoutineRequest = {
      nombre: this.nombre.trim(),
      descripcion: this.descripcion.trim() || undefined,
      ejercicios: this.exercises.map((ex, i) => ({
        ejercicioId: ex.ejercicioId,
        orden: i,
        notas: ex.notas || undefined,
        series: this.buildSeriesPayload(ex),
      })),
    };

    const request$ = this.isEdit() && this.editPublicId
      ? this.routineApi.updateRutina(this.editPublicId, payload)
      : this.routineApi.createRutina(payload);

    request$.subscribe({
      next: (saved) => {
        this.submitting.set(false);
        this.router.navigate(['/app/routines', saved.publicId]);
      },
      error: (err) => {
        this.submitting.set(false);
        const msg = (err?.error?.error as string | undefined) ?? '';
        this.errorMessage.set(msg || 'No se pudo guardar la rutina.');
      },
    });
  }

  goBack(): void {
    if (this.isEdit() && this.editPublicId) {
      this.router.navigate(['/app/routines', this.editPublicId]);
    } else {
      this.router.navigate(['/app/routines']);
    }
  }
}
