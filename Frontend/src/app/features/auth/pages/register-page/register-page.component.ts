import { NgIf } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthApiService } from '../../../../core/services/auth-api.service';

@Component({
  selector: 'app-register-page',
  imports: [RouterLink, ReactiveFormsModule, NgIf],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.scss',
})
export class RegisterPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authApiService = inject(AuthApiService);

  readonly submitting = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly passwordStrength = signal<{ label: string; percent: number }>({ label: '—', percent: 0 });

  readonly form = this.fb.nonNullable.group({
    nombre: ['', [Validators.required]],
    apellido1: ['', [Validators.required]],
    apellido2: [''],
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    repeatPassword: ['', [Validators.required]],
  }, { validators: [this.passwordsMatchValidator] });

  constructor() {
    this.form.controls.password.valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((password) => {
        this.passwordStrength.set(this.getPasswordStrength(password ?? ''));
      });
  }

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
      username: raw.username.trim(),
      email: raw.email.trim(),
      nombre: raw.nombre.trim(),
      apellido1: raw.apellido1.trim(),
      apellido2: raw.apellido2.trim() || undefined,
      password: raw.password,
    };

    this.authApiService.register(payload).subscribe({
      next: (response) => {
        this.submitting.set(false);
        if (response.success) {
          this.successMessage.set('Cuenta creada correctamente. Ya puedes iniciar sesion.');
          this.form.reset({
            nombre: '',
            apellido1: '',
            apellido2: '',
            username: '',
            email: '',
            password: '',
            repeatPassword: '',
          });
          return;
        }

        this.errorMessage.set(response.error || 'No se pudo crear la cuenta.');
      },
      error: (error) => {
        this.submitting.set(false);
        const apiMessage = (error?.error?.error as string | undefined) ?? '';
        this.errorMessage.set(apiMessage || 'No se pudo crear la cuenta. Revisa tus datos e intentalo de nuevo.');
      },
    });
  }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value as string | undefined;
    const repeatPassword = group.get('repeatPassword')?.value as string | undefined;
    return password === repeatPassword ? null : { passwordsMismatch: true };
  }

  private getPasswordStrength(password: string): { label: string; percent: number } {
    let score = 0;
    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    const levels = [
      { label: '—', percent: 0 },
      { label: 'Debil', percent: 25 },
      { label: 'Regular', percent: 50 },
      { label: 'Buena', percent: 75 },
      { label: 'Excelente', percent: 100 },
    ];

    return levels[score];
  }
}
