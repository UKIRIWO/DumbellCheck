import { Injectable, computed, signal } from '@angular/core';
import { AuthSession } from '../models/auth-session.model';

const SESSION_STORAGE_KEY = 'dc.auth.session';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly sessionSignal = signal<AuthSession | null>(this.loadSession());

  readonly session = computed(() => this.sessionSignal());
  readonly isAuthenticated = computed(() => this.sessionSignal() !== null);
  readonly role = computed(() => this.sessionSignal()?.rol ?? null);
  readonly accessToken = computed(() => this.sessionSignal()?.accessToken ?? null);
  readonly isAdmin = computed(() => this.sessionSignal()?.rol === 'ADMIN');

  setSession(session: AuthSession): void {
    this.sessionSignal.set(session);
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
  }

  clearSession(): void {
    this.sessionSignal.set(null);
    localStorage.removeItem(SESSION_STORAGE_KEY);
  }

  private loadSession(): AuthSession | null {
    const rawValue = localStorage.getItem(SESSION_STORAGE_KEY);
    if (!rawValue) {
      return null;
    }

    try {
      return JSON.parse(rawValue) as AuthSession;
    } catch {
      localStorage.removeItem(SESSION_STORAGE_KEY);
      return null;
    }
  }
}
