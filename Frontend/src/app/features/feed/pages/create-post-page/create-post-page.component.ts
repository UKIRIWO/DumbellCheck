import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PostApiService } from '../../../../core/services/post-api.service';
import { Ejercicio } from '../../../../core/models/exercise.model';
import { CreatePostRequest } from '../../../../core/models/post.model';
import { ExerciseSelectorComponent } from '../../components/exercise-selector/exercise-selector.component';

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
  selector: 'app-create-post-page',
  imports: [FormsModule, ExerciseSelectorComponent],
  templateUrl: './create-post-page.component.html',
})
export class CreatePostPageComponent {
  private readonly postApi = inject(PostApiService);
  private readonly router = inject(Router);

  titulo = '';
  descripcion = '';
  exercises: WorkingEjercicio[] = [];

  showExerciseSelector = signal(false);
  submitting = signal(false);
  errorMessage = signal('');
  selectedMediaFile: File | null = null;
  mediaPreviewUrl: string | null = null;
  mediaType: 'image' | 'video' | null = null;

  readonly isValidRest = isValidRestInput;

  get canSubmit(): boolean {
    return (
      this.titulo.trim().length > 0 &&
      this.exercises.length > 0 &&
      this.exercises.every((ex) => {
        if (ex.series.length === 0) return false;
        const firstSerie = ex.series[0];
        return (
          firstSerie.repeticiones != null &&
          firstSerie.peso != null &&
          ex.series.every((s) => isValidRestInput(s.descansoInput))
        );
      })
    );
  }

  onEjercicioSelected(ej: Ejercicio): void {
    const alreadyAdded = this.exercises.some((e) => e.ejercicioId === ej.id);
    if (!alreadyAdded) {
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

  removeSerie(ej: WorkingEjercicio, serieIdx: number): void {
    ej.series.splice(serieIdx, 1);
  }

  private newSerie(): WorkingSerie {
    return { repeticiones: null, peso: null, descansoInput: '' };
  }

  getPesoPlaceholder(ejercicio: WorkingEjercicio, serieIndex: number): string {
    if (serieIndex === 0) return '0';
    const prev = this.findPreviousValue<number>(ejercicio, serieIndex, (serie) => serie.peso);
    return prev == null ? '0' : String(prev);
  }

  getRepsPlaceholder(ejercicio: WorkingEjercicio, serieIndex: number): string {
    if (serieIndex === 0) return '0';
    const prev = this.findPreviousValue<number>(ejercicio, serieIndex, (serie) => serie.repeticiones);
    return prev == null ? '0' : String(prev);
  }

  getDescansoPlaceholder(ejercicio: WorkingEjercicio, serieIndex: number): string {
    if (serieIndex === 0) return 'seg / mm:ss';
    const prev = this.findPreviousValue<string>(ejercicio, serieIndex, (serie) => {
      const trimmed = serie.descansoInput.trim();
      return trimmed ? trimmed : null;
    });
    return prev ?? 'seg / mm:ss';
  }

  private findPreviousValue<T>(
    ejercicio: WorkingEjercicio,
    serieIndex: number,
    selector: (serie: WorkingSerie) => T | null,
  ): T | null {
    for (let i = serieIndex - 1; i >= 0; i--) {
      const value = selector(ejercicio.series[i]);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private buildSeriesPayload(ex: WorkingEjercicio): CreatePostRequest['ejercicios'][number]['series'] {
    const result: CreatePostRequest['ejercicios'][number]['series'] = [];
    let lastReps: number | null = null;
    let lastPeso: number | null = null;
    let lastDescansoSegundos: number | undefined = undefined;

    ex.series.forEach((s, sIdx) => {
      const repeticiones = s.repeticiones ?? lastReps;
      const peso = s.peso ?? lastPeso;
      const descansoParsed = parseRestInput(s.descansoInput);
      const descansoSegundos = descansoParsed ?? lastDescansoSegundos;

      if (repeticiones == null || peso == null) {
        throw new Error('Serie inválida: faltan repeticiones o peso');
      }

      result.push({
        numeroSerie: sIdx + 1,
        repeticiones,
        peso,
        descansoSegundos,
      });

      lastReps = repeticiones;
      lastPeso = peso;
      lastDescansoSegundos = descansoSegundos;
    });

    return result;
  }

  submit(): void {
    if (!this.canSubmit || this.submitting()) return;
    this.errorMessage.set('');
    this.submitting.set(true);

    const payload: CreatePostRequest = {
      titulo: this.titulo.trim(),
      descripcion: this.descripcion.trim() || undefined,
      ejercicios: this.exercises.map((ex, exIdx) => ({
        ejercicioId: ex.ejercicioId,
        orden: exIdx,
        notas: ex.notas || undefined,
        series: this.buildSeriesPayload(ex),
      })),
    };

    const createPostWithPayload = (finalPayload: CreatePostRequest) => {
      this.postApi.createPost(finalPayload).subscribe({
        next: () => {
          this.submitting.set(false);
          this.router.navigateByUrl('/app/feed');
        },
        error: (err) => {
          this.submitting.set(false);
          const msg = (err?.error?.error as string | undefined) ?? '';
          this.errorMessage.set(msg || 'No se pudo guardar el entrenamiento.');
        },
      });
    };

    if (this.selectedMediaFile) {
      this.postApi.uploadMedia(this.selectedMediaFile).subscribe({
          next: (mediaUrl) => {
            createPostWithPayload({ ...payload, multimediaUrl: mediaUrl });
          },
          error: (err) => {
            this.submitting.set(false);
            const msg = (err?.error?.error as string | undefined) ?? '';
            this.errorMessage.set(msg || 'No se pudo subir el archivo multimedia.');
          },
        });
      return;
    }

    createPostWithPayload(payload);
  }

  goBack(): void {
    this.router.navigateByUrl('/app/feed');
  }

  onMediaSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    if (!file) {
      this.clearMedia();
      return;
    }

    const allowed = file.type.startsWith('image/') || file.type.startsWith('video/');
    if (!allowed) {
      this.errorMessage.set('Solo se permiten archivos de imagen o video.');
      input.value = '';
      this.clearMedia();
      return;
    }

    const maxSize = 20 * 1024 * 1024;
    if (file.size > maxSize) {
      this.errorMessage.set('El archivo no puede superar los 20MB.');
      input.value = '';
      this.clearMedia();
      return;
    }

    this.errorMessage.set('');
    this.selectedMediaFile = file;
    this.mediaType = file.type.startsWith('video/') ? 'video' : 'image';
    this.mediaPreviewUrl = URL.createObjectURL(file);
  }

  removeMedia(): void {
    this.clearMedia();
  }

  private clearMedia(): void {
    if (this.mediaPreviewUrl) {
      URL.revokeObjectURL(this.mediaPreviewUrl);
    }
    this.selectedMediaFile = null;
    this.mediaPreviewUrl = null;
    this.mediaType = null;
  }
}
