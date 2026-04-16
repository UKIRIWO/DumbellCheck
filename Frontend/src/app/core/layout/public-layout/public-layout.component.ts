import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-public-layout',
  imports: [RouterOutlet],
  template: `
    <main class="mx-auto flex min-h-screen w-full max-w-md flex-col justify-center px-4 py-6">
      <router-outlet />
    </main>
  `,
})
export class PublicLayoutComponent {}
