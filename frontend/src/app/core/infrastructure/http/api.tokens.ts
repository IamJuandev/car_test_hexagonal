import { InjectionToken } from '@angular/core';

/** Base URL of the Spring Boot API. Overridable at bootstrap for other environments. */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => 'http://localhost:8080',
});
