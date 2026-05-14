import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/admin-shell/admin-shell').then((m) => m.AdminShell),
    children: [
      { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
      {
        path: 'usuarios',
        loadComponent: () =>
          import('./pages/admin-users-page/admin-users-page').then((m) => m.AdminUsersPage),
      },
      {
        path: 'publicaciones',
        loadComponent: () =>
          import('./pages/admin-posts-page/admin-posts-page').then((m) => m.AdminPostsPage),
      },
      {
        path: 'comentarios',
        loadComponent: () =>
          import('./pages/admin-comments-page/admin-comments-page').then((m) => m.AdminCommentsPage),
      },
      {
        path: 'baneos',
        loadComponent: () =>
          import('./pages/admin-bans-page/admin-bans-page').then((m) => m.AdminBansPage),
      },
      {
        path: 'ejercicios',
        loadComponent: () =>
          import('./pages/admin-exercises-page/admin-exercises-page').then(
            (m) => m.AdminExercisesPage,
          ),
      },
    ],
  },
];
