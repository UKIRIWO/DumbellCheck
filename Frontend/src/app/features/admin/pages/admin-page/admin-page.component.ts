import { HttpClient } from '@angular/common/http';
import { NgIf } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { API_BASE_URL } from '../../../../core/tokens/api-base-url.token';
import { ApiResponse } from '../../../../core/models/api-response.model';

@Component({
  selector: 'app-admin-page',
  imports: [NgIf],
  template: `
    <section class="rounded-2xl bg-white p-6 shadow-sm">
      <h1 class="font-heading text-2xl font-bold text-brand-text">Zona admin</h1>
      <p class="mt-2 text-sm text-brand-gray">
        Esta pantalla solo debe cargar si el usuario autenticado tiene rol ADMIN.
      </p>

      <button
        class="mt-4 rounded-lg border border-blue-700 bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
        type="button"
        [disabled]="loading()"
        (click)="checkAdminAccess()"
      >
        {{ loading() ? 'Comprobando...' : 'Comprobar acceso admin en backend' }}
      </button>

      <p *ngIf="successMessage()" class="mt-3 text-sm text-green-700">{{ successMessage() }}</p>
      <p *ngIf="errorMessage()" class="mt-3 text-sm text-red-700">{{ errorMessage() }}</p>
    </section>
  `,
})
export class AdminPageComponent {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = inject(API_BASE_URL);

  readonly loading = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');

  constructor() {
    this.checkAdminAccess();
  }

  checkAdminAccess(): void {
    this.loading.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    this.http.get<ApiResponse<{ message: string }>>(`${this.apiBaseUrl}/admin/check`).subscribe({
      next: (response) => {
        this.loading.set(false);
        if (response.success) {
          this.successMessage.set(response.data.message);
          return;
        }

        this.errorMessage.set(response.error || 'No se pudo validar el acceso admin.');
      },
      error: (error) => {
        this.loading.set(false);
        const apiMessage = (error?.error?.error as string | undefined) ?? '';
        this.errorMessage.set(apiMessage || 'No se pudo validar el acceso admin.');
      },
    });
  }
}
