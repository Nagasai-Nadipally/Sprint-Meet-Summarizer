import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  {
    path: 'upload',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/upload/upload.component').then((m) => m.UploadComponent),
  },
  {
    path: 'meetings/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/meeting-detail/meeting-detail.component').then(
        (m) => m.MeetingDetailComponent,
      ),
  },
  { path: '**', redirectTo: 'dashboard' },
];
