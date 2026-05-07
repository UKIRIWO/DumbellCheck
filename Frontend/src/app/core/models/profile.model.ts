export type Plataforma = 'instagram' | 'youtube' | 'twitter' | 'tiktok' | 'web' | 'otro';

export interface UsuarioEnlace {
  id: number;
  plataforma: Plataforma;
  url: string;
  orden: number;
}

export interface Perfil {
  id: number;
  username: string;
  nombre: string;
  apellido1: string;
  apellido2?: string;
  fotoPerfilUrl?: string;
  bannerUrl?: string;
  biografia?: string;
  contadorSeguidores: number;
  contadorSeguidos: number;
  contadorPublicaciones: number;
  fechaCreacion: string;
  enlaces: UsuarioEnlace[];
  esPropio: boolean;
  sigueAEsteUsuario: boolean;
}

export interface UpdateProfileRequest {
  nombre: string;
  apellido1: string;
  apellido2?: string;
  biografia?: string;
}

export interface CreateLinkRequest {
  plataforma: Plataforma;
  url: string;
}
