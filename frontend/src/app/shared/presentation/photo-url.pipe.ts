import { Pipe, PipeTransform, inject } from '@angular/core';
import { API_BASE_URL } from '../../core/infrastructure/http/api.tokens';

/**
 * Resolves a car's photoUrl for display.
 *
 * <p>A pasted link is already absolute and is used as is. An uploaded photo is
 * stored as a path relative to the API host ({@code /uploads/x.png}), so the
 * database never holds a hostname and the same rows keep working when the API
 * moves to another domain.
 */
@Pipe({ name: 'photoUrl', standalone: true })
export class PhotoUrlPipe implements PipeTransform {
  private readonly apiBaseUrl = inject(API_BASE_URL);

  transform(photoUrl: string | null | undefined): string | null {
    if (!photoUrl) {
      return null;
    }
    const isAbsolute = /^(https?:)?\/\//i.test(photoUrl) || photoUrl.startsWith('data:');
    return isAbsolute ? photoUrl : `${this.apiBaseUrl}${photoUrl}`;
  }
}
