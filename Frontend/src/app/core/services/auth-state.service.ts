import { Injectable, computed, signal } from '@angular/core';
import { AuthSession } from '../models/auth-session.model';

const SESSION_STORAGE_KEY = 'dc.auth.session';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly sessionSignal = signal<AuthSession | null>(this.loadSession());

  readonly session = computed(() => this.sessionSignal());
  readonly isAuthenticated = computed(() => this.getValidSession(this.sessionSignal()) !== null);
  readonly role = computed(() => this.sessionSignal()?.rol ?? null);
  readonly accessToken = computed(() => this.sessionSignal()?.accessToken ?? null);
  readonly isAdmin = computed(() => this.sessionSignal()?.rol === 'ADMIN');

  setSession(session: AuthSession): void {
    const validSession = this.getValidSession(session);
    if (!validSession) {
      this.clearSession();
      return;
    }

    this.sessionSignal.set(validSession);
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(validSession));
  }

  clearSession(): void {
    this.sessionSignal.set(null);
    localStorage.removeItem(SESSION_STORAGE_KEY);
  }

  ensureValidSession(): boolean {
    const session = this.sessionSignal();
    if (!session) {
      return false;
    }

    if (!this.getValidSession(session)) {
      this.clearSession();
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
      return this.getValidSession(parsedSession);
    } catch {
      localStorage.removeItem(SESSION_STORAGE_KEY);
      return null;
    }
  }

  private getValidSession(session: AuthSession | null): AuthSession | null {
    if (!session || !session.accessToken) {
      return null;
    }

    const payload = this.getTokenPayload(session.accessToken);
    if (!payload || typeof payload.exp !== 'number') {
      return null;
    }

    const expiresAtMs = payload.exp * 1000;
    if (Date.now() >= expiresAtMs) {
      return null;
    }

    return session;
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
