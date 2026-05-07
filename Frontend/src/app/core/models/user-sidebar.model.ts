export interface SidebarProfile {
  id: number;
  username: string;
  nombre: string;
  fotoPerfilUrl: string | null;
  contadorSeguidores: number;
  contadorSeguidos: number;
  contadorPublicaciones: number;
}

export interface SidebarSuggestion {
  id: number;
  username: string;
  nombre: string;
  fotoPerfilUrl: string | null;
}

export interface SidebarData {
  perfil: SidebarProfile;
  sugerencias: SidebarSuggestion[];
}

