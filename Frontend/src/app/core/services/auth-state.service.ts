import { Injectable, computed, signal } from '@angular/core';
import { AuthSession } from '../models/auth-session.model';

const SESSION_STORAGE_KEY = 'dc.auth.session';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly sessionSignal = signal<AuthSession | null>(this.loadSession());

  readonly session = computed(() => this.sessionSignal());
  readonly isAuthenticated = computed(() => this.ensureActiveSession());
  readonly role = computed(() => this.sessionSignal()?.rol ?? null);
  readonly accessToken = computed(() => this.sessionSignal()?.accessToken ?? null);
  readonly refreshToken = computed(() => this.sessionSignal()?.refreshToken ?? null);
  readonly isAdmin = computed(() => this.sessionSignal()?.rol === 'ADMIN');

  setSession(session: AuthSession): void {
    if (!this.isSessionShapeValid(session)) {
      this.clearSession();
      return;
    }

    this.sessionSignal.set(session);
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
  }

  clearSession(): void {
    this.sessionSignal.set(null);
    localStorage.removeItem(SESSION_STORAGE_KEY);
  }

  ensureValidSession(): boolean {
    return this.ensureActiveSession();
  }

  hasValidAccessToken(): boolean {
    const session = this.sessionSignal();
    return this.isTokenValid(session?.accessToken);
  }

  hasValidRefreshToken(): boolean {
    const session = this.sessionSignal();
    return this.isTokenValid(session?.refreshToken);
  }

  getRefreshToken(): string | null {
    const session = this.sessionSignal();
    return session?.refreshToken ?? null;
  }

  private ensureActiveSession(): boolean {
    const session = this.sessionSignal();
    if (!session) {
      return false;
    }

    if (!this.isSessionShapeValid(session)) {
      this.clearSession();
      return false;
    }

    if (this.hasValidAccessToken() || this.hasValidRefreshToken()) {
      return true;
    }

    this.clearSession();
    return false;
  }

  private isSessionShapeValid(session: AuthSession | null): boolean {
    if (!session) {
      return false;
    }

    if (!session.accessToken || !session.refreshToken) {
      return false;
    }

    return true;
  }

  private loadSession(): AuthSession | null {
    const rawValue = localStorage.getItem(SESSION_STORAGE_KEY);
    if (!rawValue) {
      return null;
    }

    try {
      const parsedSession = JSON.parse(rawValue) as AuthSession;
      if (!this.isSessionShapeValid(parsedSession)) {
        localStorage.removeItem(SESSION_STORAGE_KEY);
        return null;
      }

      return parsedSession;
    } catch {
      localStorage.removeItem(SESSION_STORAGE_KEY);
      return null;
    }
  }

  private isTokenValid(token: string | undefined): boolean {
    if (!token) {
      return false;
    }

    const payload = this.getTokenPayload(token);
    if (!payload || typeof payload.exp !== 'number') {
      return false;
    }

    const expiresAtMs = payload.exp * 1000;
    return Date.now() < expiresAtMs;
  }

  private getTokenPayload(token: string): { exp?: number } | null {
    const tokenParts = token.split('.');
    if (tokenParts.length !== 3) {
      return null;
    }

    try {
      const normalizedPayload = tokenParts[1].replace(/-/g, '+').replace(/_/g, '/');
      const decodedPayload = atob(normalizedPayload);
      return JSON.parse(decodedPayload) as { exp?: number };
    } catch {
      return null;
    }
  }
}
