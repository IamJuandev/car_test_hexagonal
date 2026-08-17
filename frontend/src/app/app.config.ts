import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { AuthRepositoryPort } from './core/application/ports/auth.repository.port';
import { CarRepositoryPort } from './core/application/ports/car.repository.port';
import { PhotoRepositoryPort } from './core/application/ports/photo.repository.port';
import { SessionStoragePort } from './core/application/ports/session-storage.port';
import { LocalStorageSessionAdapter } from './core/infrastructure/auth/local-storage-session.adapter';
import { AuthHttpAdapter } from './core/infrastructure/http/auth-http.adapter';
import { CarHttpAdapter } from './core/infrastructure/http/car-http.adapter';
import { PhotoHttpAdapter } from './core/infrastructure/http/photo-http.adapter';
import { jwtInterceptor } from './core/infrastructure/http/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    { provide: AuthRepositoryPort, useClass: AuthHttpAdapter },
    { provide: CarRepositoryPort, useClass: CarHttpAdapter },
    { provide: PhotoRepositoryPort, useClass: PhotoHttpAdapter },
    { provide: SessionStoragePort, useClass: LocalStorageSessionAdapter },
  ],
};
