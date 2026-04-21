import { Component } from '@angular/core';
import { LogoutConfirmButton } from '../logout-confirm-button/logout-confirm-button';

@Component({
  selector: 'app-header',
  imports: [LogoutConfirmButton],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {}
