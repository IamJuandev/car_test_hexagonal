import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Car } from '../../../core/domain/car.model';
import {
  DeleteCarUseCase,
  ListCarsUseCase,
} from '../../../core/application/use-cases/car.use-cases';
import { CarFormComponent } from './car-form.component';

@Component({
  selector: 'app-car-list',
  standalone: true,
  imports: [RouterLink, FormsModule, CarFormComponent],
  template: `
    <section class="garage-page" aria-labelledby="garage-title">
      <header class="page-heading">
        <div>
          <span class="eyebrow">Vehicle overview</span>
          <h1 id="garage-title">My garage</h1>
          <p>Keep every vehicle detail clear, current, and easy to find.</p>
        </div>
        <button type="button" class="button button--primary" (click)="openModal()">
          <svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 5v14M5 12h14" />
          </svg>
          <span>Add car</span>
        </button>
      </header>

      @if (!loading() && cars().length > 0) {
        <div class="garage-toolbar" role="search">
          <div class="search-field">
            <svg class="search-field__icon" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="11" cy="11" r="7" />
              <path d="m16 16 4 4" />
            </svg>
            <label class="sr-only" for="car-search">Search cars</label>
            <input
              id="car-search"
              type="search"
              [ngModel]="query()"
              (ngModelChange)="query.set($event)"
              placeholder="Search brand, model, plate, or color"
              autocomplete="off"
            />
            @if (query()) {
              <button
                type="button"
                class="search-field__clear"
                aria-label="Clear car search"
                (click)="query.set('')"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="m7 7 10 10M17 7 7 17" />
                </svg>
              </button>
            }
          </div>
          <p class="garage-toolbar__count" aria-live="polite">
            <span>{{ filteredCars().length }}</span>
            {{ filteredCars().length === 1 ? 'vehicle' : 'vehicles' }}
          </p>
        </div>
      }

      @if (loading()) {
        <div class="loading-state" role="status" aria-live="polite">
          <div class="loading-state__heading">
            <span class="loading-spinner" aria-hidden="true"></span>
            <span>Loading your garage…</span>
          </div>
          <ul class="card-grid card-grid--loading" aria-hidden="true">
            @for (item of [1, 2, 3]; track item) {
              <li class="car-card car-card--skeleton">
                <div class="skeleton skeleton--media"></div>
                <div class="car-card__body">
                  <div class="skeleton skeleton--title"></div>
                  <div class="skeleton skeleton--line"></div>
                  <div class="skeleton skeleton--plate"></div>
                </div>
              </li>
            }
          </ul>
        </div>
      } @else if (cars().length === 0) {
        <div class="empty-state">
          <div class="empty-state__icon" aria-hidden="true">
            <svg viewBox="0 0 48 48">
              <path d="M9 28h30v10H9zM13 28l4-11h14l4 11M15 38v3M33 38v3" />
              <circle cx="16" cy="33" r="2" />
              <circle cx="32" cy="33" r="2" />
            </svg>
          </div>
          <span class="eyebrow">Your garage is ready</span>
          <h2>Add your first car</h2>
          <p>Save the details you need so they are always close at hand.</p>
          <button type="button" class="button button--primary" (click)="openModal()">
            <svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 5v14M5 12h14" />
            </svg>
            <span>Add first car</span>
          </button>
        </div>
      } @else if (filteredCars().length === 0) {
        <div class="empty-state empty-state--compact" role="status">
          <div class="empty-state__icon empty-state__icon--search" aria-hidden="true">
            <svg viewBox="0 0 48 48">
              <circle cx="21" cy="21" r="11" />
              <path d="m29 29 10 10M18 18l6 6M24 18l-6 6" />
            </svg>
          </div>
          <h2>No matching vehicles</h2>
          <p>Nothing matches “{{ query() }}”. Try another term or clear the search.</p>
          <button type="button" class="button button--secondary" (click)="query.set('')">
            Clear search
          </button>
        </div>
      } @else {
        <ul class="card-grid" aria-label="Your vehicles">
          @for (car of filteredCars(); track car.id) {
            <li class="car-card">
              <div class="car-card__media">
                @if (car.photoUrl) {
                  <img
                    [src]="car.photoUrl"
                    [alt]="car.brand + ' ' + car.model"
                    loading="lazy"
                  />
                } @else {
                  <span class="car-card__placeholder" aria-hidden="true">
                    <svg viewBox="0 0 64 64">
                      <path d="M10 35h44v14H10zM16 35l6-15h20l6 15M17 49v5M47 49v5" />
                      <circle cx="20" cy="42" r="3" />
                      <circle cx="44" cy="42" r="3" />
                    </svg>
                  </span>
                }
                <span class="car-card__year">{{ car.year }}</span>
              </div>

              <div class="car-card__body">
                <p class="car-card__brand">{{ car.brand }}</p>
                <h2 class="car-card__title">{{ car.model }}</h2>
                <dl class="car-card__details">
                  <div>
                    <dt>Color</dt>
                    <dd>
                      <span class="color-dot" aria-hidden="true"></span>
                      {{ car.color }}
                    </dd>
                  </div>
                  <div>
                    <dt>Plate</dt>
                    <dd class="car-card__plate">{{ car.plateNumber }}</dd>
                  </div>
                </dl>
              </div>

              <div class="car-card__actions">
                <a
                  class="button button--card"
                  [routerLink]="['/cars', car.id, 'edit']"
                  [attr.aria-label]="'Edit ' + car.brand + ' ' + car.model"
                >
                  <svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="m4 16-.8 4 4-.8L18 8.4 15.6 6zM14 7.6l2.4 2.4" />
                  </svg>
                  <span>Edit</span>
                </a>
                <button
                  type="button"
                  class="button button--card button--danger-quiet"
                  (click)="remove(car)"
                  [attr.aria-label]="'Delete ' + car.brand + ' ' + car.model"
                >
                  <svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M5 7h14M9 7V4h6v3M8 10v7M12 10v7M16 10v7M7 7l1 13h8l1-13" />
                  </svg>
                  <span>Delete</span>
                </button>
              </div>
            </li>
          }
        </ul>
      }
    </section>

    @if (modalOpen()) {
      <div
        class="modal-backdrop"
        [class.is-closing]="closing()"
        (click)="closeModal()"
        (keydown.escape)="closeModal()"
      >
        <section
          class="modal-panel"
          [class.is-closing]="closing()"
          role="dialog"
          aria-modal="true"
          aria-labelledby="add-car-title"
          (click)="$event.stopPropagation()"
          (animationend)="onPanelAnimationEnd()"
        >
          <header class="modal-panel__header">
            <div>
              <span class="eyebrow">New vehicle</span>
              <h2 id="add-car-title">Add a car</h2>
              <p>Enter the core details for this vehicle.</p>
            </div>
            <button
              type="button"
              class="icon-button"
              aria-label="Close add car dialog"
              (click)="closeModal()"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="m6 6 12 12M18 6 6 18" />
              </svg>
            </button>
          </header>
          <app-car-form [modal]="true" (saved)="onSaved()" (closed)="closeModal()" />
        </section>
      </div>
    }
  `,
})
export class CarListComponent implements OnInit {
  private readonly listCars = inject(ListCarsUseCase);
  private readonly deleteCar = inject(DeleteCarUseCase);

  protected readonly cars = signal<Car[]>([]);
  protected readonly loading = signal(true);
  protected readonly query = signal('');
  protected readonly modalOpen = signal(false);
  protected readonly closing = signal(false);

  protected readonly filteredCars = computed(() => {
    const term = this.query().trim().toLowerCase();
    if (!term) {
      return this.cars();
    }
    return this.cars().filter((car) =>
      [car.brand, car.model, car.plateNumber, car.color]
        .some((field) => field.toLowerCase().includes(term)),
    );
  });

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.listCars.execute().subscribe({
      next: (cars) => {
        this.cars.set(cars);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  openModal(): void {
    this.closing.set(false);
    this.modalOpen.set(true);
  }

  /** Start the close animation; the panel is removed on animationend. */
  closeModal(): void {
    this.closing.set(true);
  }

  onPanelAnimationEnd(): void {
    if (this.closing()) {
      this.modalOpen.set(false);
      this.closing.set(false);
    }
  }

  onSaved(): void {
    this.closeModal();
    this.load();
  }

  remove(car: Car): void {
    if (!confirm(`Delete ${car.brand} ${car.model}?`)) {
      return;
    }
    this.deleteCar.execute(car.id).subscribe({
      next: () => this.cars.update((list) => list.filter((c) => c.id !== car.id)),
    });
  }
}
