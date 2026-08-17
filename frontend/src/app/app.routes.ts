import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './shared/presentation/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'cars' },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/presentation/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/presentation/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'cars',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/cars/presentation/car-list.component').then((m) => m.CarListComponent),
  },
  {
    path: 'cars/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/cars/presentation/car-form.component').then((m) => m.CarFormComponent),
  },
  {
    path: 'cars/:id/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/cars/presentation/car-form.component').then((m) => m.CarFormComponent),
  },
  { path: '**', redirectTo: 'cars' },
];
