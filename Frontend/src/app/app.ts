import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AccessibilityWidget } from './shared/components/accessibility-widget/accessibility-widget';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AccessibilityWidget],
  template: `
    <router-outlet />
    <app-accessibility-widget />
  `,
})
export class App {}
