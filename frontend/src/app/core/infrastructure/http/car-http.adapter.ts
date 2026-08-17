import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CarRepositoryPort } from '../../application/ports/car.repository.port';
import { Car, CarInput } from '../../domain/car.model';
import { API_BASE_URL } from './api.tokens';

/** Talks to /api/cars on the backend. The JWT is attached by the interceptor. */
@Injectable()
export class CarHttpAdapter extends CarRepositoryPort {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${inject(API_BASE_URL)}/api/cars`;

  override list(): Observable<Car[]> {
    return this.http.get<Car[]>(this.endpoint);
  }

  override getById(id: number): Observable<Car> {
    return this.http.get<Car>(`${this.endpoint}/${id}`);
  }

  override create(input: CarInput): Observable<Car> {
    return this.http.post<Car>(this.endpoint, input);
  }

  override update(id: number, input: CarInput): Observable<Car> {
    return this.http.put<Car>(`${this.endpoint}/${id}`, input);
  }

  override remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }
}
