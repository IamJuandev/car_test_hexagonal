import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionStoragePort } from '../../application/ports/session-storage.port';

export const jwtInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(SessionStoragePort).token();
  return next(
    token
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request,
  );
};
