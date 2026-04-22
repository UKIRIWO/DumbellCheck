import { Component, input } from '@angular/core';

@Component({
  selector: 'app-auth-brand-panel',
  imports: [],
  templateUrl: './auth-brand-panel.html',
  styleUrl: './auth-brand-panel.scss',
})
export class AuthBrandPanel {
  readonly mode = input<'login' | 'register'>('login');
}
