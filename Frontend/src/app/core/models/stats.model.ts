export interface WeeklyActivity {
  semana: string;
  isNewMonth: boolean;
  cantidad: number;
}

export interface VolumeByGroup {
  grupo: string;
  volumenKg: number;
}

export interface ExerciseFrequency {
  ejercicio: string;
  imagenUrl?: string;
  veces: number;
}

export interface ExerciseBestLift {
  ejercicio: string;
  imagenUrl?: string;
  maxPesoKg: number;
}

export interface StatsData {
  totalWorkouts: number;
  totalVolumenKg: number;
  totalSeries: number;
  totalRepeticiones: number;
  actividadSemanal: WeeklyActivity[];
  volumenPorGrupo: VolumeByGroup[];
  ejerciciosMasFrecuentes: ExerciseFrequency[];
  mejoresMarcas: ExerciseBestLift[];
}
