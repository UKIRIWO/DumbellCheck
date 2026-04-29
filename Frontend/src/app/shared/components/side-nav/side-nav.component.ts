import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LogoutConfirmButton } from '../logout-confirm-button/logout-confirm-button';
import { AuthStateService } from '../../../core/services/auth-state.service';

@Component({
  selector: 'app-side-nav',
  imports: [RouterLink, RouterLinkActive, LogoutConfirmButton],
  templateUrl: './side-nav.component.html',
})
export class SideNavComponent {
  private readonly authStateService = inject(AuthStateService);

  readonly username = computed(() => this.authStateService.session()?.username ?? '');

  readonly avatarLetter = computed(() => (this.username() ? this.username().charAt(0).toUpperCase() : 'U'));
}

