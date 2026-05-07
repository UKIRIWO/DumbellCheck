import { Component, OnInit, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CurrentUserService } from '../../../../core/services/current-user.service';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-feed-right-sidebar',
  imports: [RouterLink, UserAvatarComponent],
  templateUrl: './feed-right-sidebar.html',
  standalone: true,
})
export class FeedRightSidebar implements OnInit {
  private readonly currentUser = inject(CurrentUserService);

  readonly loading = this.currentUser.loading;
  readonly errorMessage = this.currentUser.errorMessage;
  readonly profile = this.currentUser.profile;
  readonly suggestions = this.currentUser.suggestions;
  readonly profileInitial = computed(() => this.profile()?.username?.charAt(0).toUpperCase() ?? 'U');

  ngOnInit(): void {
    this.currentUser.ensureLoaded(8).subscribe({ error: () => {} });
  }
}
