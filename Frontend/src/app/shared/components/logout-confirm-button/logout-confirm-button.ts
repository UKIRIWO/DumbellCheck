import { Component, ElementRef, inject, input, output, viewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStateService } from '../../../core/services/auth-state.service';

@Component({
  selector: 'app-logout-confirm-button',
  imports: [],
  templateUrl: './logout-confirm-button.html',
  styleUrl: './logout-confirm-button.scss',
})
export class LogoutConfirmButton {
  private readonly authStateService = inject(AuthStateService);
  private readonly router = inject(Router);
  private readonly confirmDialog = viewChild<ElementRef<HTMLDialogElement>>('confirmDialog');

  readonly buttonText = input('Cerrar sesion');
  readonly buttonIconClass = input('');
  readonly buttonClass = input('rounded-full bg-blue px-4 py-2 font-heading text-sm font-semibold text-white');
  readonly modalTitle = input('Confirmar cierre de sesion');
  readonly modalMessage = input('Estas seguro de que quieres cerrar sesion?');
  readonly confirmText = input('Cerrar sesion');
  readonly cancelText = input('Cancelar');
  readonly redirectTo = input('/auth/login');

  readonly loggedOut = output<void>();

  openModal(): void {
    this.confirmDialog()?.nativeElement.showModal();
  }

  closeModal(): void {
    this.confirmDialog()?.nativeElement.close();
  }

  onDialogClick(event: MouseEvent): void {
    const dialogElement = this.confirmDialog()?.nativeElement;
    if (!dialogElement) {
      return;
    }

    const rect = dialogElement.getBoundingClientRect();
    const isOutside =
      event.clientX < rect.left ||
      event.clientX > rect.right ||
      event.clientY < rect.top ||
      event.clientY > rect.bottom;

    if (isOutside) {
      this.closeModal();
    }
  }

  confirmLogout(): void {
    this.authStateService.clearSession();
    this.closeModal();
    this.loggedOut.emit();
    this.router.navigateByUrl(this.redirectTo());
  }
}
