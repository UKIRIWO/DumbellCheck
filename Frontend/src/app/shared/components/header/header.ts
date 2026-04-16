import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthStateService } from '../../../core/services/auth-state.service';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  private readonly authStateService = inject(AuthStateService);
  private readonly router = inject(Router);

  logout(): void {
    this.authStateService.clearSession();
    this.router.navigateByUrl('/auth/login');
  }
}
