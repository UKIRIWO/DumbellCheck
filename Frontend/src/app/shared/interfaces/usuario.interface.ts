export interface Usuario {
  id: number;
  username: string;
  email: string;
  nombre: string;
  apellido1: string;
  apellido2?: string;
  foto_perfil_url?: string;
  biografia?: string;
  rol: string;
  esta_activo: boolean;
}
