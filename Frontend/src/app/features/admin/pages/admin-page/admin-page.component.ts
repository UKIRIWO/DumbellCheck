import { HttpClient } from '@angular/common/http';
import { NgIf } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { API_BASE_URL } from '../../../../core/tokens/api-base-url.token';
import { ApiResponse } from '../../../../core/models/api-response.model';

@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [NgIf],
  templateUrl: './admin-page.component.html',
  styleUrl: './admin-page.component.scss',
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
