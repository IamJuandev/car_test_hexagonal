import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CurrentSessionUseCase } from '../../core/application/use-cases/auth.use-cases';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  return inject(CurrentSessionUseCase).isAuthenticated()
    ? true
    : router.createUrlTree(['/login']);
};

/** Prevents authenticated users from returning to login or registration pages. */
export const guestGuard: CanActivateFn = () => {
  const router = inject(Router);
  return inject(CurrentSessionUseCase).isAuthenticated()
    ? router.createUrlTree(['/cars'])
    : true;
};
