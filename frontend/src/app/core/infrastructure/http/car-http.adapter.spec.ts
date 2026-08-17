import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CarHttpAdapter } from './car-http.adapter';
import { API_BASE_URL } from './api.tokens';
import { Car } from '../../domain/car.model';

describe('CarHttpAdapter', () => {
  let adapter: CarHttpAdapter;
  let httpMock: HttpTestingController;
  const base = 'http://api.test';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CarHttpAdapter,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_BASE_URL, useValue: base },
      ],
    });
    adapter = TestBed.inject(CarHttpAdapter);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('GETs the cars collection', () => {
    const cars: Car[] = [
      { id: 1, brand: 'Toyota', model: 'Corolla', year: 2020, plateNumber: 'ABC123', color: 'red', photoUrl: null },
    ];

    let received: Car[] = [];
    adapter.list().subscribe((c) => (received = c));

    const req = httpMock.expectOne(`${base}/api/cars`);
    expect(req.request.method).toBe('GET');
    req.flush(cars);

    expect(received).toEqual(cars);
  });

  it('POSTs a new car to the collection', () => {
    const input = { brand: 'Honda', model: 'Civic', year: 2021, plateNumber: 'XYZ', color: 'blue', photoUrl: null };
    adapter.create(input).subscribe();

    const req = httpMock.expectOne(`${base}/api/cars`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(input);
    req.flush({ id: 9, ...input });
  });

  it('DELETEs a car by id', () => {
    adapter.remove(9).subscribe();
    const req = httpMock.expectOne(`${base}/api/cars/9`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
