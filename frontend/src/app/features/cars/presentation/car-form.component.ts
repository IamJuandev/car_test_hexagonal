import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CreateCarUseCase,
  GetCarUseCase,
  UpdateCarUseCase,
  UploadCarPhotoUseCase,
} from '../../../core/application/use-cases/car.use-cases';
import { PhotoUrlPipe } from '../../../shared/presentation/photo-url.pipe';

type PhotoSource = 'upload' | 'link';

@Component({
  selector: 'app-car-form',
  standalone: true,
  imports: [ReactiveFormsModule, PhotoUrlPipe],
  template: `
    <section class="car-form-page" [class.car-form-page--modal]="modal">
      @if (!modal) {
        <header class="page-heading page-heading--form">
          <div>
            <span class="eyebrow">{{ carId() ? 'Vehicle details' : 'New vehicle' }}</span>
            <h1>{{ carId() ? 'Edit car' : 'Add a car' }}</h1>
            <p>
              {{ carId()
                ? 'Keep this vehicle’s information accurate and up to date.'
                : 'Add the essential information for this vehicle.' }}
            </p>
          </div>
        </header>
      }

      <form
        class="vehicle-form"
        [class.vehicle-form--card]="!modal"
        [formGroup]="form"
        (ngSubmit)="submit()"
        novalidate
      >
        <fieldset class="form-section">
          <legend>
            <span class="form-section__number" aria-hidden="true">01</span>
            Vehicle identity
          </legend>
          <p class="form-section__description">How you recognize this car at a glance.</p>

          <div class="form-grid">
            <div class="field">
              <label for="car-brand">
                Brand <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <input
                id="car-brand"
                type="text"
                formControlName="brand"
                autocomplete="organization"
                placeholder="e.g. Toyota"
                required
                autofocus
                [attr.aria-invalid]="form.controls.brand.invalid && form.controls.brand.touched"
                [attr.aria-describedby]="form.controls.brand.invalid && form.controls.brand.touched ? 'car-brand-error' : null"
              />
              @if (form.controls.brand.invalid && form.controls.brand.touched) {
                <p class="field-error" id="car-brand-error">Brand is required.</p>
              }
            </div>

            <div class="field">
              <label for="car-model">
                Model <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <input
                id="car-model"
                type="text"
                formControlName="model"
                placeholder="e.g. Corolla"
                required
                [attr.aria-invalid]="form.controls.model.invalid && form.controls.model.touched"
                [attr.aria-describedby]="form.controls.model.invalid && form.controls.model.touched ? 'car-model-error' : null"
              />
              @if (form.controls.model.invalid && form.controls.model.touched) {
                <p class="field-error" id="car-model-error">Model is required.</p>
              }
            </div>

            <div class="field">
              <label for="car-year">
                Year <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <input
                id="car-year"
                type="number"
                formControlName="year"
                min="1886"
                inputmode="numeric"
                required
                [attr.aria-invalid]="form.controls.year.invalid && form.controls.year.touched"
                [attr.aria-describedby]="form.controls.year.invalid && form.controls.year.touched ? 'car-year-error' : null"
              />
              @if (form.controls.year.invalid && form.controls.year.touched) {
                <p class="field-error" id="car-year-error">Enter a year from 1886 onward.</p>
              }
            </div>

            <div class="field">
              <label for="car-color">
                Color <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <input
                id="car-color"
                type="text"
                formControlName="color"
                placeholder="e.g. Midnight blue"
                required
                [attr.aria-invalid]="form.controls.color.invalid && form.controls.color.touched"
                [attr.aria-describedby]="form.controls.color.invalid && form.controls.color.touched ? 'car-color-error' : null"
              />
              @if (form.controls.color.invalid && form.controls.color.touched) {
                <p class="field-error" id="car-color-error">Color is required.</p>
              }
            </div>
          </div>
        </fieldset>

        <fieldset class="form-section">
          <legend>
            <span class="form-section__number" aria-hidden="true">02</span>
            Registration and photo
          </legend>
          <p class="form-section__description">Details that make this vehicle uniquely yours.</p>

          <div class="form-grid">
            <div class="field">
              <label for="car-plate">
                Plate number <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <input
                id="car-plate"
                class="plate-input"
                type="text"
                formControlName="plateNumber"
                placeholder="ABC123"
                autocomplete="off"
                required
                [attr.aria-invalid]="form.controls.plateNumber.invalid && form.controls.plateNumber.touched"
                [attr.aria-describedby]="form.controls.plateNumber.invalid && form.controls.plateNumber.touched ? 'car-plate-error' : 'car-plate-hint'"
              />
              @if (form.controls.plateNumber.invalid && form.controls.plateNumber.touched) {
                <p class="field-error" id="car-plate-error">Plate number is required.</p>
              } @else {
                <p class="field-hint" id="car-plate-hint">Use the official plate format.</p>
              }
            </div>

            <div class="field field--photo">
              <span class="field-label" id="car-photo-label">
                Photo <span class="optional-label">Optional</span>
              </span>

              <div class="photo-source" role="tablist" aria-labelledby="car-photo-label">
                <button
                  type="button"
                  role="tab"
                  class="photo-source__tab"
                  [class.is-active]="photoSource() === 'upload'"
                  [attr.aria-selected]="photoSource() === 'upload'"
                  (click)="selectSource('upload')"
                >
                  Upload a file
                </button>
                <button
                  type="button"
                  role="tab"
                  class="photo-source__tab"
                  [class.is-active]="photoSource() === 'link'"
                  [attr.aria-selected]="photoSource() === 'link'"
                  (click)="selectSource('link')"
                >
                  Paste a link
                </button>
              </div>

              @if (photoSource() === 'upload') {
                <input
                  id="car-photo-file"
                  class="photo-file-input"
                  type="file"
                  accept="image/jpeg,image/png,image/webp,image/gif"
                  (change)="onFileSelected($event)"
                  [disabled]="uploading()"
                  aria-describedby="car-photo-hint"
                />
                <p class="field-hint" id="car-photo-hint">
                  JPEG, PNG, WebP or GIF, up to 5 MB.
                </p>
                @if (uploading()) {
                  <p class="field-hint" role="status">Uploading…</p>
                }
              } @else {
                <input
                  id="car-photo"
                  type="url"
                  formControlName="photoUrl"
                  placeholder="https://example.com/car.jpg"
                  inputmode="url"
                  aria-describedby="car-photo-link-hint"
                />
                <p class="field-hint" id="car-photo-link-hint">Paste a public image link.</p>
              }

              @if (photoError()) {
                <p class="field-error" role="alert">{{ photoError() }}</p>
              }

              @if (form.controls.photoUrl.value; as currentPhoto) {
                <figure class="photo-preview">
                  <img [src]="currentPhoto | photoUrl" alt="Selected car photo" />
                  <figcaption>
                    <button type="button" class="link-button" (click)="clearPhoto()">
                      Remove photo
                    </button>
                  </figcaption>
                </figure>
              }
            </div>
          </div>
        </fieldset>

        @if (error()) {
          <div class="notice notice--error" role="alert">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="12" cy="12" r="9" />
              <path d="M12 7v6M12 17h.01" />
            </svg>
            <span>{{ error() }}</span>
          </div>
        }

        <footer class="form-actions">
          <p><span aria-hidden="true">*</span> Required fields</p>
          <div class="form-actions__buttons">
            <button type="button" class="button button--secondary" (click)="cancel()">
              Cancel
            </button>
            <button
              type="submit"
              class="button button--primary"
              [disabled]="form.invalid || loading()"
              [attr.aria-busy]="loading()"
            >
              @if (loading()) {
                <span class="button-spinner" aria-hidden="true"></span>
              } @else {
                <svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M5 12.5 9.2 17 19 7" />
                </svg>
              }
              <span>{{ loading() ? 'Saving…' : (carId() ? 'Save changes' : 'Add car') }}</span>
            </button>
          </div>
        </footer>
      </form>
    </section>
  `,
})
export class CarFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly getCar = inject(GetCarUseCase);
  private readonly createCar = inject(CreateCarUseCase);
  private readonly updateCar = inject(UpdateCarUseCase);
  private readonly uploadPhoto = inject(UploadCarPhotoUseCase);

  /** When true the component renders inline (inside a modal) and emits instead of navigating. */
  @Input() modal = false;
  @Output() readonly saved = new EventEmitter<void>();
  @Output() readonly closed = new EventEmitter<void>();

  protected readonly carId = signal<number | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly photoSource = signal<PhotoSource>('upload');
  protected readonly uploading = signal(false);
  protected readonly photoError = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    brand: ['', [Validators.required]],
    model: ['', [Validators.required]],
    year: [new Date().getFullYear(), [Validators.required, Validators.min(1886)]],
    plateNumber: ['', [Validators.required]],
    color: ['', [Validators.required]],
    photoUrl: [''],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.carId.set(id);
      this.getCar.execute(id).subscribe({
        next: (car) => {
          // Reopen the tab that matches how this photo was set: an absolute URL
          // was pasted, a relative one came from an upload.
          if (car.photoUrl?.startsWith('http')) {
            this.photoSource.set('link');
          }
          this.form.patchValue({
            brand: car.brand,
            model: car.model,
            year: car.year,
            plateNumber: car.plateNumber,
            color: car.color,
            photoUrl: car.photoUrl ?? '',
          });
        },
        error: () => this.error.set('Could not load the car'),
      });
    }
  }

  selectSource(source: PhotoSource): void {
    this.photoSource.set(source);
    this.photoError.set(null);
  }

  /**
   * Uploads the picked file and stores the returned URL in the same photoUrl
   * control the link tab writes to, so submitting the form is identical either
   * way.
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.photoError.set(null);
    this.uploading.set(true);
    this.uploadPhoto.execute(file).subscribe({
      next: (url) => {
        this.form.patchValue({ photoUrl: url });
        this.uploading.set(false);
        input.value = '';
      },
      error: (err) => {
        this.photoError.set(
          err?.status === 400 || err?.status === 413
            ? 'Use a JPEG, PNG, WebP or GIF image of up to 5 MB'
            : 'Could not upload the image',
        );
        this.uploading.set(false);
        input.value = '';
      },
    });
  }

  clearPhoto(): void {
    this.form.patchValue({ photoUrl: '' });
    this.photoError.set(null);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const input = { ...value, photoUrl: value.photoUrl.trim() === '' ? null : value.photoUrl.trim() };

    this.loading.set(true);
    this.error.set(null);
    const id = this.carId();
    const request$ = id
      ? this.updateCar.execute(id, input)
      : this.createCar.execute(input);

    request$.subscribe({
      next: () => this.finish(),
      error: (err) => {
        this.error.set(err?.status === 409 ? 'You already have a car with that plate' : 'Could not save the car');
        this.loading.set(false);
      },
    });
  }

  cancel(): void {
    if (this.modal) {
      this.closed.emit();
      return;
    }
    this.router.navigate(['/cars']);
  }

  private finish(): void {
    if (this.modal) {
      this.saved.emit();
      return;
    }
    this.router.navigate(['/cars']);
  }
}
