import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { CarListComponent } from './car-list.component';
import { Car } from '../../../core/domain/car.model';
import {
  DeleteCarUseCase,
  ListCarsUseCase,
} from '../../../core/application/use-cases/car.use-cases';

describe('CarListComponent (search/filter)', () => {
  const cars: Car[] = [
    { id: 1, brand: 'Toyota', model: 'Corolla', year: 2021, plateNumber: 'ABC123', color: 'Silver', photoUrl: null },
    { id: 2, brand: 'Ford', model: 'Focus', year: 2019, plateNumber: 'XYZ789', color: 'Blue', photoUrl: null },
    { id: 3, brand: 'Honda', model: 'Civic', year: 2020, plateNumber: 'TOY001', color: 'Red', photoUrl: null },
  ];

  function createComponent() {
    TestBed.configureTestingModule({
      imports: [CarListComponent],
      providers: [
        provideRouter([]),
        { provide: ListCarsUseCase, useValue: { execute: () => of(cars) } },
        { provide: DeleteCarUseCase, useValue: { execute: () => of(void 0) } },
      ],
    });
    const fixture = TestBed.createComponent(CarListComponent);
    fixture.detectChanges();
    return fixture.componentInstance as any;
  }

  it('returns all cars when the query is empty', () => {
    const component = createComponent();
    expect(component.filteredCars().length).toBe(3);
  });

  it('filters by brand, case-insensitively', () => {
    const component = createComponent();
    component.query.set('ford');
    expect(component.filteredCars().map((c: Car) => c.id)).toEqual([2]);
  });

  it('matches the plate number as well as the brand', () => {
    const component = createComponent();
    component.query.set('toy');
    // "Toyota" (brand) and "TOY001" (plate) both match.
    expect(component.filteredCars().map((c: Car) => c.id).sort()).toEqual([1, 3]);
  });

  it('returns nothing when no field matches', () => {
    const component = createComponent();
    component.query.set('zzz');
    expect(component.filteredCars().length).toBe(0);
  });

  it('opens the add-car modal', () => {
    const component = createComponent();
    expect(component.modalOpen()).toBe(false);
    component.openModal();
    expect(component.modalOpen()).toBe(true);
    expect(component.closing()).toBe(false);
  });

  it('removes the modal only after the close animation ends', () => {
    const component = createComponent();
    component.openModal();
    component.closeModal();
    // close animation requested but panel still mounted
    expect(component.closing()).toBe(true);
    expect(component.modalOpen()).toBe(true);
    // animationend fires -> panel is removed
    component.onPanelAnimationEnd();
    expect(component.modalOpen()).toBe(false);
    expect(component.closing()).toBe(false);
  });
});
