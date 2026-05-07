import { Component, Input } from '@angular/core';
import { Plataforma } from '../../../../core/models/profile.model';

@Component({
  selector: 'app-profile-link-icon',
  imports: [],
  templateUrl: './profile-link-icon.html',
})
export class ProfileLinkIcon {
  @Input({ required: true }) plataforma!: Plataforma;

  get iconClass(): string {
    const map: Record<Plataforma, string> = {
      instagram: 'mdi mdi-instagram',
      youtube: 'mdi mdi-youtube',
      twitter: 'mdi mdi-twitter',
      tiktok: 'mdi mdi-music-note',
      web: 'mdi mdi-web',
      otro: 'mdi mdi-link-variant',
    };
    return map[this.plataforma] ?? 'mdi mdi-link-variant';
  }
}
