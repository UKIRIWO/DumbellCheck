import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-auth-brand-panel',
  imports: [RouterLink],
  templateUrl: './auth-brand-panel.html',
  styleUrl: './auth-brand-panel.scss',
})
export class AuthBrandPanel {
  readonly mode = input<'login' | 'register'>('login');
}
