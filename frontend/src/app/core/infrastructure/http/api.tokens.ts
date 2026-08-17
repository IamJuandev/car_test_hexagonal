import { InjectionToken } from '@angular/core';
import { environment } from '../../../../environments/environment';

/**
 * Base URL of the Spring Boot API. Empty in the deployed build, where the API
 * answers on the same host under /api. Overridable at bootstrap, which is what
 * the adapter tests use.
 */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => environment.apiBaseUrl,
});
