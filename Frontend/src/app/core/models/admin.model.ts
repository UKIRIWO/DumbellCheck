export type RolUsuario = 'MEMBER' | 'ADMIN' | 'SUPPORT';

export interface AdminBan {
  id: number;
  usuarioId: number;
  usuarioUsername?: string;
  baneadoHasta?: string;
  baneadoPermanentemente: boolean;
  motivoBaneo?: string;
  fechaCreacion: string;
}

export interface AdminUser {
  id: number;
  username: string;
  email: string;
  nombre: string;
  apellido1: string;
  apellido2?: string;
  fotoPerfilUrl?: string;
  biografia?: string;
  rol: RolUsuario;
  contadorSeguidores: number;
  contadorSeguidos: number;
  baneos: AdminBan[];
  estaActivo: boolean;
  fechaCreacion: string;
  ultimaConexion?: string;
}

export interface AdminUserUpdateRequest {
  rol?: RolUsuario;
  estaActivo?: boolean;
}

export interface AdminPost {
  id: number;
  publicId: string;
  usuarioUsername: string;
  titulo: string;
  contadorLikes: number;
  contadorComentarios: number;
  multimediaUrl?: string;
  estaActiva: boolean;
  fechaCreacion: string;
}

export interface AdminComment {
  id: number;
  usuarioUsername?: string;
  texto: string;
  publicacionId: number;
  estaActivo: boolean;
  contadorLikes: number;
  fechaCreacion: string;
}

export interface AdminBanCreateRequest {
  usuarioId: number;
  motivoBaneo?: string;
  baneadoHasta?: string;
  baneadoPermanentemente: boolean;
}

export interface AdminBanUpdateRequest {
  motivoBaneo?: string;
  baneadoHasta?: string;
  baneadoPermanentemente: boolean;
}

export interface AdminEjercicio {
  id: number;
  nombre: string;
  descripcion?: string;
  imagenUrl?: string;
  fechaCreacion: string;
}

export interface AdminEjercicioRequest {
  nombre: string;
  descripcion?: string;
  imagenUrl?: string;
}

export interface AdminPageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
}
