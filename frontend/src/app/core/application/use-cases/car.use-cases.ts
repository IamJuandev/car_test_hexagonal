import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CarRepositoryPort } from '../ports/car.repository.port';
import { PhotoRepositoryPort } from '../ports/photo.repository.port';
import { Car, CarInput } from '../../domain/car.model';

@Injectable({ providedIn: 'root' })
export class ListCarsUseCase {
  private readonly cars = inject(CarRepositoryPort);

  execute(): Observable<Car[]> {
    return this.cars.list();
  }
}

@Injectable({ providedIn: 'root' })
export class GetCarUseCase {
  private readonly cars = inject(CarRepositoryPort);

  execute(id: number): Observable<Car> {
    return this.cars.getById(id);
  }
}

@Injectable({ providedIn: 'root' })
export class CreateCarUseCase {
  private readonly cars = inject(CarRepositoryPort);

  execute(input: CarInput): Observable<Car> {
    return this.cars.create(input);
  }
}

@Injectable({ providedIn: 'root' })
export class UpdateCarUseCase {
  private readonly cars = inject(CarRepositoryPort);

  execute(id: number, input: CarInput): Observable<Car> {
    return this.cars.update(id, input);
  }
}

@Injectable({ providedIn: 'root' })
export class DeleteCarUseCase {
  private readonly cars = inject(CarRepositoryPort);

  execute(id: number): Observable<void> {
    return this.cars.remove(id);
  }
}

/**
 * Uploads an image and returns the URL to store on the car. Kept separate from
 * saving the car so both ways of setting a photo — uploading a file and pasting
 * a link — end up writing the same single field.
 */
@Injectable({ providedIn: 'root' })
export class UploadCarPhotoUseCase {
  private readonly photos = inject(PhotoRepositoryPort);

  execute(file: File): Observable<string> {
    return this.photos.upload(file);
  }
}
