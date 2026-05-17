export interface ChatParticipant {
  usuarioId: number;
  username: string;
  nombre: string;
  fotoPerfilUrl?: string;
  rol: string;
}

export interface ChatParticipantResumen {
  usuarioId: number;
  username: string;
  fotoPerfilUrl?: string;
}

export interface ChatLastMessage {
  tipoMensaje: string;
  contenido?: string;
  remitenteUsername: string;
  esMio: boolean;
  fechaCreacion: string;
}

export interface ChatListItem {
  publicId: string;
  nombre: string;
  tipo: string;
  fotoUrl?: string;
  ultimoMensaje?: ChatLastMessage;
  mensajesNoLeidos: number;
  fechaUltimaActividad: string;
  participantes: ChatParticipantResumen[];
}

export interface ChatDetail {
  publicId: string;
  nombre: string;
  tipo: string;
  fotoUrl?: string;
  esGrupo: boolean;
  soyAdmin: boolean;
  participantes: ChatParticipant[];
}

export interface ChatMessage {
  id: number;
  chatPublicId: string;
  usuarioId: number;
  username: string;
  fotoPerfilUrl?: string;
  tipoMensaje: string;
  contenido?: string;
  archivoUrl?: string;
  rutinaId?: number;
  rutinaNombre?: string;
  rutinaPublicId?: string;
  mensajeReferenciaId?: number;
  mensajeReferenciaPreview?: string;
  mensajeReferenciaUsername?: string;
  estaEditado: boolean;
  eliminado: boolean;
  esMio: boolean;
  fechaCreacion: string;
  fechaEdicion?: string;
}

export interface CreateGroupChatRequest {
  usernames: string[];
  nombre?: string;
}

export interface AddGroupMembersRequest {
  usernames: string[];
}

export interface SendMessageRequest {
  tipoMensaje: string;
  contenido?: string;
  archivoUrl?: string;
  rutinaId?: number;
  mensajeReferenciaId?: number;
}

export interface UpdateMessageRequest {
  contenido: string;
}

export interface ChatSearchUser {
  id: number;
  username: string;
  nombre: string;
  fotoPerfilUrl?: string;
  sigo: boolean;
}

export interface UpdateChatRequest {
  nombre?: string;
}

export interface UpdateParticipantRoleRequest {
  rol: 'admin' | 'miembro';
}

export interface CursorPageResponse<T> {
  content: T[];
  nextCursor: number | null;
  hasMore: boolean;
}
