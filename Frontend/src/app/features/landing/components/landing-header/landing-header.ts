import { Component, HostListener, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthStateService } from '../../../../core/services/auth-state.service';
import { LogoutConfirmButton } from '../../../../shared/components/logout-confirm-button/logout-confirm-button';

@Component({
  selector: 'app-landing-header',
  imports: [RouterLink, LogoutConfirmButton],
  templateUrl: './landing-header.html',
  styleUrl: './landing-header.scss',
})
export class LandingHeader {
  private readonly authStateService = inject(AuthStateService);

  readonly menuOpen = signal(false);
  readonly scrolled = signal(false);
  readonly isAuthenticated = this.authStateService.isAuthenticated;

  @HostListener('window:scroll')
  onWindowScroll(): void {
    this.scrolled.set(window.scrollY > 20);
  }

  toggleMenu(): void {
    this.menuOpen.update((value) => !value);
  }

  onLoggedOut(): void {
    this.menuOpen.set(false);
  }
}
