import { Component, input } from '@angular/core';
import { AuthBrandPanel } from '../auth-brand-panel/auth-brand-panel';

@Component({
  selector: 'app-auth-card',
  imports: [AuthBrandPanel],
  templateUrl: './auth-card.html',
  styleUrl: './auth-card.scss',
})
export class AuthCard {
  readonly mode = input<'login' | 'register'>('login');
}
