import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { PhotoRepositoryPort } from '../../application/ports/photo.repository.port';
import { API_BASE_URL } from './api.tokens';

interface PhotoUploadResponse {
  photoUrl: string;
}

/** Uploads car photos to /api/cars/photos. The JWT is attached by the interceptor. */
@Injectable()
export class PhotoHttpAdapter extends PhotoRepositoryPort {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${inject(API_BASE_URL)}/api/cars/photos`;

  override upload(file: File): Observable<string> {
    const body = new FormData();
    body.append('file', file);

    // No Content-Type header on purpose: the browser adds it together with the
    // multipart boundary, and setting it by hand breaks the request.
    return this.http
      .post<PhotoUploadResponse>(this.endpoint, body)
      .pipe(map((response) => response.photoUrl));
  }
}
