import { Component, OnInit, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LogoutConfirmButton } from '../../../../shared/components/logout-confirm-button/logout-confirm-button';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { AuthStateService } from '../../../../core/services/auth-state.service';

@Component({
  selector: 'app-admin-side-nav',
  imports: [RouterLink, RouterLinkActive, LogoutConfirmButton, UserAvatarComponent],
  templateUrl: './admin-side-nav.html',
})
export class AdminSideNav implements OnInit {
  private readonly currentUser = inject(CurrentUserService);
  private readonly authState = inject(AuthStateService);

  readonly username = computed(
    () => this.currentUser.profile()?.username ?? this.authState.session()?.username ?? '',
  );
  readonly fotoPerfilUrl = computed(() => this.currentUser.profile()?.fotoPerfilUrl ?? null);

  ngOnInit(): void {
    this.currentUser.ensureLoaded().subscribe({ error: () => {} });
  }
}
