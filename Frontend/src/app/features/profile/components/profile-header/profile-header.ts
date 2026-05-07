import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { Perfil } from '../../../../core/models/profile.model';
import { ProfileLinkIcon } from '../profile-link-icon/profile-link-icon';

@Component({
  selector: 'app-profile-header',
  imports: [ProfileLinkIcon, SlicePipe],
  templateUrl: './profile-header.html',
})
export class ProfileHeader {
  @Input({ required: true }) perfil!: Perfil;
  @Output() editClick = new EventEmitter<void>();
  @Output() followClick = new EventEmitter<void>();
  @Output() messageClick = new EventEmitter<void>();

  get avatarLetter(): string {
    return this.perfil.username.charAt(0).toUpperCase();
  }
}
