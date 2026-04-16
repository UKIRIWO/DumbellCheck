import { Routes } from '@angular/router';

export const CHATS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/chats-page/chats-page.component').then((m) => m.ChatsPageComponent),
  },
];
