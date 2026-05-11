export interface CommentUserSummary {
  id: number;
  username: string;
  fotoPerfilUrl: string | null;
}

export interface Comment {
  id: number;
  comentarioPadreId: number | null;
  texto: string;
  contadorLikes: number;
  fechaCreacion: string;
  usuario: CommentUserSummary;
  respuestas: Comment[];
}

export interface CreateCommentRequest {
  texto: string;
  comentarioPadreId?: number | null;
}
