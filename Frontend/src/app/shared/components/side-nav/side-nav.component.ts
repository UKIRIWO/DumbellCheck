import { Component, OnInit, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LogoutConfirmButton } from '../logout-confirm-button/logout-confirm-button';
import { UserAvatarComponent } from '../user-avatar/user-avatar.component';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { CurrentUserService } from '../../../core/services/current-user.service';

@Component({
  selector: 'app-side-nav',
  imports: [RouterLink, RouterLinkActive, LogoutConfirmButton, UserAvatarComponent],
  templateUrl: './side-nav.component.html',
})
export class SideNavComponent implements OnInit {
  private readonly authStateService = inject(AuthStateService);
  private readonly currentUser = inject(CurrentUserService);

  readonly profile = this.currentUser.profile;

  readonly username = computed(
    () => this.profile()?.username ?? this.authStateService.session()?.username ?? '',
  );

  readonly fotoPerfilUrl = computed(() => this.profile()?.fotoPerfilUrl ?? null);

  readonly profileLink = computed<unknown[]>(() => {
    const u = this.username();
    return u ? ['/app/profile', u] : ['/app/profile'];
  });

  ngOnInit(): void {
    this.currentUser.ensureLoaded().subscribe({ error: () => {} });
  }
}

