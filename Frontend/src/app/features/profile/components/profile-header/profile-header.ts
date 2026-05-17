import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { Perfil } from '../../../../core/models/profile.model';
import { ProfileLinkIcon } from '../profile-link-icon/profile-link-icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-profile-header',
  imports: [ProfileLinkIcon, SlicePipe, RouterLink],
  templateUrl: './profile-header.html',
})
export class ProfileHeader {
  @Input({ required: true }) perfil!: Perfil;
  @Input() followLoading = false;
  @Output() editClick = new EventEmitter<void>();
  @Output() followClick = new EventEmitter<void>();
  @Output() seguidoresClick = new EventEmitter<void>();
  @Output() seguidosClick = new EventEmitter<void>();
  @Output() messageClick = new EventEmitter<void>();

  get avatarLetter(): string {
    return this.perfil.username.charAt(0).toUpperCase();
  }
}
