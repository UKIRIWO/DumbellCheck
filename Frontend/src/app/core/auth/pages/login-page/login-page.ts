import { NgIf } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApiService } from '../../../services/auth-api.service';
import { AuthStateService } from '../../../services/auth-state.service';
import { AuthCard } from '../../components/auth-card/auth-card';

@Component({
  selector: 'app-login-page',
  imports: [RouterLink, NgIf, ReactiveFormsModule, AuthCard],
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
})
export class LoginPage {
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
