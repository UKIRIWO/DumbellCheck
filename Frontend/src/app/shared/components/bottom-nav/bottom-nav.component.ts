import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-bottom-nav',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="fixed bottom-0 left-0 right-0 border-t bg-white p-3">
      <ul class="mx-auto flex max-w-md items-center justify-between text-sm font-medium text-brand-text">
        <li><a routerLink="/app/feed" routerLinkActive="text-primary-dark">Feed</a></li>
        <li><a routerLink="/app/routines" routerLinkActive="text-primary-dark">Routines</a></li>
        <li><a routerLink="/app/chats" routerLinkActive="text-primary-dark">Chats</a></li>
        <li><a routerLink="/app/notifications" routerLinkActive="text-primary-dark">Alerts</a></li>
        <li><a routerLink="/app/profile" routerLinkActive="text-primary-dark">Profile</a></li>
      </ul>
    </nav>
  `,
})
export class BottomNavComponent {}
