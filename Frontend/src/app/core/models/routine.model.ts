import { EjercicioEnPost } from './post.model';

export interface RoutineUserSummary {
  id: number;
  username: string;
  fotoPerfilUrl?: string | null;
}

export interface RoutineSerieResponse {
  numeroSerie: number;
  repeticiones: number;
  peso: number;
  descansoSegundos?: number | null;
  rpe?: number | null;
  notas?: string | null;
}

export interface RoutineEjercicioResponse {
  id: number;
  ejercicioId: number;
  nombre: string;
  imagenUrl?: string | null;
  orden: number;
  notas?: string | null;
  series: RoutineSerieResponse[];
}

export interface Routine {
  id: number;
  publicId: string;
  nombre: string;
  descripcion?: string | null;
  esPublica: boolean;
  fechaCreacion: string;
  usuario: RoutineUserSummary;
  ejercicios: RoutineEjercicioResponse[];
}

export interface RoutineListItem {
  id: number;
  publicId: string;
  nombre: string;
  descripcion?: string | null;
  esPublica: boolean;
  fechaCreacion: string;
  usuario: RoutineUserSummary;
  nombresEjercicios: string[];
}

export interface CreateRoutineSerieRequest {
  numeroSerie: number;
  repeticiones: number;
  peso: number;
  descansoSegundos?: number | null;
  rpe?: number | null;
  notas?: string | null;
}

export interface CreateRoutineEjercicioRequest {
  ejercicioId: number;
  orden?: number;
  notas?: string;
  series: CreateRoutineSerieRequest[];
}

export interface CreateRoutineRequest {
  nombre: string;
  descripcion?: string;
  ejercicios: CreateRoutineEjercicioRequest[];
}

/** Converts Routine exercises to the EjercicioEnPost shape used by WorkoutDetailExercises. */
export function routineEjerciciosToPostShape(ejercicios: RoutineEjercicioResponse[]): EjercicioEnPost[] {
  return ejercicios.map((ej, idx) => ({
    id: ej.id,
    ejercicioId: ej.ejercicioId,
    nombre: ej.nombre,
    imagenUrl: ej.imagenUrl ?? undefined,
    orden: ej.orden ?? idx,
    notas: ej.notas ?? undefined,
    series: ej.series.map((s) => ({
      numeroSerie: s.numeroSerie,
      repeticiones: s.repeticiones,
      peso: s.peso,
      descansoSegundos: s.descansoSegundos ?? undefined,
    })),
  }));
}
