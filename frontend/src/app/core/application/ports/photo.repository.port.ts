import { Observable } from 'rxjs';

/**
 * Outbound port for image storage. The component that picks a file never learns
 * whether the bytes travel over HTTP, so the upload target can change without
 * touching the UI.
 */
export abstract class PhotoRepositoryPort {
  /** Uploads the image and resolves to the URL to store as the car's photoUrl. */
  abstract upload(file: File): Observable<string>;
}
