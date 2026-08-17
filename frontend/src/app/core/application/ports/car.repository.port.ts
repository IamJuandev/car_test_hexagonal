import { Observable } from 'rxjs';
import { Car, CarInput } from '../../domain/car.model';

/** Outbound port for car data. Implemented by an HTTP adapter in infrastructure. */
export abstract class CarRepositoryPort {
  abstract list(): Observable<Car[]>;
  abstract getById(id: number): Observable<Car>;
  abstract create(input: CarInput): Observable<Car>;
  abstract update(id: number, input: CarInput): Observable<Car>;
  abstract remove(id: number): Observable<void>;
}
