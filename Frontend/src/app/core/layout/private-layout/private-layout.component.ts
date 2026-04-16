import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BottomNavComponent } from '../../../shared/components/bottom-nav/bottom-nav.component';
import { Header } from '../../../shared/components/header/header';

@Component({
  selector: 'app-private-layout',
  imports: [RouterOutlet, BottomNavComponent, Header],
  template: `
    <main class="mx-auto min-h-screen w-full max-w-md px-4 pb-20 pt-6">
      <app-header />
      <router-outlet />
    </main>
    <app-bottom-nav />
  `,
})
export class PrivateLayoutComponent {}
