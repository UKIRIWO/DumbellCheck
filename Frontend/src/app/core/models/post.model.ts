export interface CreateSerieRequest {
  numeroSerie: number;
  repeticiones: number;
  peso: number;
  descansoSegundos?: number;
  rpe?: number;
  notas?: string;
}

export interface CreateEjercicioRequest {
  ejercicioId: number;
  orden?: number;
  notas?: string;
  series: CreateSerieRequest[];
}

export interface CreatePostRequest {
  titulo: string;
  descripcion?: string;
  multimediaUrl?: string;
  ejercicios: CreateEjercicioRequest[];
}

export interface UsuarioResumen {
  id: number;
  username: string;
  fotoPerfilUrl?: string;
}

export interface SerieEnPost {
  numeroSerie: number;
  repeticiones: number;
  peso: number;
  descansoSegundos?: number;
}

export interface EjercicioEnPost {
  id: number;
  ejercicioId: number;
  nombre: string;
  imagenUrl?: string;
  orden: number;
  notas?: string;
  series: SerieEnPost[];
}

export interface PostFeedItem {
  id: number;
  usuario: UsuarioResumen;
  titulo: string;
  descripcion?: string;
  multimediaUrl?: string;
  contadorLikes: number;
  contadorComentarios: number;
  fechaCreacion: string;
  ejercicios: EjercicioEnPost[];
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  last: boolean;
}
