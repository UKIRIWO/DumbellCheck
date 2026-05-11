export interface CommentUserSummary {
  id: number;
  username: string;
  fotoPerfilUrl: string | null;
}

export interface Comment {
  id: number;
  comentarioPadreId: number | null;
  texto: string | null;
  contadorLikes: number;
  meGusta: boolean;
  eliminado: boolean;
  fechaCreacion: string;
  usuario: CommentUserSummary | null;
  mencionesValidas: string[];
  respuestas: Comment[];
}

export interface CreateCommentRequest {
  texto: string;
  comentarioPadreId?: number | null;
}
