export interface CommentUserSummary {
  id: number;
  username: string;
  fotoPerfilUrl: string | null;
}

export interface Comment {
  id: number;
  texto: string;
  contadorLikes: number;
  fechaCreacion: string;
  usuario: CommentUserSummary;
}

export interface CreateCommentRequest {
  texto: string;
}
