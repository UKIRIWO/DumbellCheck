import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SidebarProfile } from '../../../../core/models/user-sidebar.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-sidebar-profile-card',
  imports: [RouterLink, UserAvatarComponent],
  templateUrl: './sidebar-profile-card.html',
  styleUrl: './sidebar-profile-card.scss',
})
export class SidebarProfileCard {
  @Input({ required: true }) profile!: SidebarProfile;
  @Input({ required: true }) ctaLink!: (string | number)[];
  @Input({ required: true }) ctaLabel!: string;
}
