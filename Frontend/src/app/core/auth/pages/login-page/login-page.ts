import { DatePipe, NgIf } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../../services/auth-api.service';
import { AuthStateService } from '../../../services/auth-state.service';
import { AuthCard } from '../../components/auth-card/auth-card';
import { PasswordField } from '../../../../shared/components/password-field/password-field';

interface BanData {
  motivoBaneo?: string;
  baneadoHasta?: string;
  baneadoPermanentemente: boolean;
}

@Component({
  selector: 'app-login-page',
  imports: [RouterLink, NgIf, DatePipe, ReactiveFormsModule, AuthCard, PasswordField],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authApiService = inject(AuthApiService);
  private readonly authStateService = inject(AuthStateService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly banData = signal<BanData | null>(null);

  ngOnInit(): void {
    // Ban data is passed via router navigation state — only present right after being kicked
    const state = (window.history.state ?? {}) as { banData?: BanData };
    if (state.banData) {
      this.banData.set(state.banData);
    }
  }

  readonly form = this.fb.nonNullable.group({
    principal: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set('');
    this.successMessage.set('');
    this.submitting.set(true);

    const raw = this.form.getRawValue();
    const payload = {
      principal: raw.principal.trim(),
      password: raw.password,
    };

    this.authApiService.login(payload).subscribe({
      next: (response) => {
        this.submitting.set(false);
        if (response.success) {
          this.authStateService.setSession(response.data);
          this.successMessage.set('Sesion iniciada correctamente.');
          const targetUrl = response.data.rol === 'ADMIN' ? '/app/admin' : '/app/feed';
          this.router.navigateByUrl(targetUrl);
          return;
        }

        this.errorMessage.set(response.error || 'Credenciales incorrectas.');
      },
      error: (error) => {
        this.submitting.set(false);
        const errorCode = error?.error?.errorCode as string | undefined;
        if (errorCode === 'USER_BANNED') {
          this.banData.set(error.error.data as BanData);
          return;
        }
        const apiMessage = (error?.error?.error as string | undefined) ?? '';
        this.errorMessage.set(apiMessage || 'Credenciales incorrectas.');
      },
    });
  }
}
