export interface AuthSession {
  accessToken: string;
  refreshToken?: string;
  usuarioId: number;
  username: string;
  rol: 'MEMBER' | 'ADMIN' | 'SUPPORT';
}
