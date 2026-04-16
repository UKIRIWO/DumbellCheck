import { NgIf } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../../../core/services/auth-api.service';
import { AuthStateService } from '../../../../core/services/auth-state.service';

@Component({
  selector: 'app-login-page',
  imports: [RouterLink, NgIf, ReactiveFormsModule],
  template: `
    <section class="rounded-2xl bg-white p-6 shadow-sm">
      <h1 class="font-heading text-2xl font-bold text-brand-text">Iniciar sesion</h1>
      <p class="mt-2 text-sm text-brand-gray">Accede con tu usuario o email y contrasena.</p>

      <form class="mt-5 grid gap-3" [formGroup]="form" (ngSubmit)="submit()">
        <label class="text-xs font-semibold uppercase tracking-wide text-brand-text">Usuario o email</label>
        <input
          class="rounded-lg border p-2"
          type="text"
          placeholder="usuario o email"
          formControlName="principal"
        />

        <label class="text-xs font-semibold uppercase tracking-wide text-brand-text">Contrasena</label>
        <input
          class="rounded-lg border p-2"
          type="password"
          placeholder="Tu contrasena"
          formControlName="password"
        />

        <button
          class="mt-1 rounded-lg border border-blue-700 bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
          type="submit"
          [disabled]="form.invalid || submitting()"
        >
          {{ submitting() ? 'Validando...' : 'Entrar' }}
        </button>
      </form>

      <p *ngIf="successMessage()" class="mt-3 text-sm text-green-700">{{ successMessage() }}</p>
      <p *ngIf="errorMessage()" class="mt-3 text-sm text-red-700">{{ errorMessage() }}</p>

      <a class="mt-4 inline-block text-sm text-brand-blue" routerLink="/auth/register">Create account</a>
    </section>
  `,
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authApiService = inject(AuthApiService);
  private readonly authStateService = inject(AuthStateService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

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
        const apiMessage = (error?.error?.error as string | undefined) ?? '';
        this.errorMessage.set(apiMessage || 'Credenciales incorrectas.');
      },
    });
  }
}
