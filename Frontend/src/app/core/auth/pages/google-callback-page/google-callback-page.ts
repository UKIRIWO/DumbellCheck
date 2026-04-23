import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthStateService } from '../../../services/auth-state.service';
import { AuthSession } from '../../../models/auth-session.model';

@Component({
  selector: 'app-google-callback-page',
  templateUrl: './google-callback-page.html',
  styleUrl: './google-callback-page.scss',
})
export class GoogleCallbackPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authStateService = inject(AuthStateService);

  readonly loadingMessage = signal('Procesando inicio de sesion con Google...');

  constructor() {
    this.processGoogleCallback();
  }

  private processGoogleCallback(): void {
    const query = this.route.snapshot.queryParamMap;
    const accessToken = query.get('accessToken');
    const usuarioIdRaw = query.get('usuarioId');
    const username = query.get('username');
    const rol = query.get('rol');
    const googleError = query.get('googleError');

    if (googleError) {
      this.router.navigate(['/auth/login'], { queryParams: { googleError } });
      return;
    }

    const usuarioId = usuarioIdRaw ? Number(usuarioIdRaw) : NaN;
    if (!accessToken || !username || !rol || Number.isNaN(usuarioId)) {
      this.router.navigate(['/auth/login'], {
        queryParams: { googleError: 'Respuesta de Google incompleta' },
      });
      return;
    }

    if (rol !== 'MEMBER' && rol !== 'ADMIN' && rol !== 'SUPPORT') {
      this.router.navigate(['/auth/login'], {
        queryParams: { googleError: 'Rol de usuario no valido' },
      });
      return;
    }

    const session: AuthSession = {
      accessToken,
      usuarioId,
      username,
      rol,
    };

    this.authStateService.setSession(session);
    const targetUrl = rol === 'ADMIN' ? '/app/admin' : '/app/feed';
    void this.router.navigateByUrl(targetUrl);
  }
}
