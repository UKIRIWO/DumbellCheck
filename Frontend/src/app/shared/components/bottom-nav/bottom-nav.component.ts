import { Component, OnInit, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { CurrentUserService } from '../../../core/services/current-user.service';

@Component({
  selector: 'app-bottom-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './bottom-nav.component.html',
})
export class BottomNavComponent implements OnInit {
  private readonly authStateService = inject(AuthStateService);
  private readonly currentUser = inject(CurrentUserService);

  readonly isAdmin = this.authStateService.isAdmin;

  readonly profileLink = computed<unknown[]>(() => {
    const u =
      this.currentUser.profile()?.username ??
      this.authStateService.session()?.username ??
      '';
    return u ? ['/app/profile', u] : ['/app/profile'];
  });

  ngOnInit(): void {
    this.currentUser.ensureLoaded().subscribe({ error: () => {} });
  }
}
