import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { RegisterUseCase } from '../../../core/application/use-cases/auth.use-cases';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-layout" aria-labelledby="register-title">
      <aside class="auth-story auth-story--register" aria-label="Car Manager overview">
        <span class="eyebrow eyebrow--light">Start your digital garage</span>
        <h1>Vehicle management<br />without the clutter.</h1>
        <p>
          Create a private workspace for the cars you own and the details you
          reference most.
        </p>
        <div class="auth-story__stat" aria-label="Designed for fast vehicle management">
          <span class="auth-story__stat-value">One</span>
          <span class="auth-story__stat-label">simple place for every vehicle</span>
        </div>
      </aside>

      <div class="auth-panel">
        <div class="auth-panel__heading">
          <span class="eyebrow">Create your account</span>
          <h2 id="register-title">Set up your garage</h2>
          <p>It only takes a moment. You can add your first car right after.</p>
        </div>

        <form class="auth-form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <div class="field">
            <label for="register-name">Full name</label>
            <div class="input-shell">
              <svg class="input-shell__icon" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="8" r="4" />
                <path d="M5 20a7 7 0 0 1 14 0" />
              </svg>
              <input
                id="register-name"
                type="text"
                formControlName="name"
                autocomplete="name"
                placeholder="Your name"
                [attr.aria-invalid]="form.controls.name.invalid && form.controls.name.touched"
                [attr.aria-describedby]="form.controls.name.invalid && form.controls.name.touched ? 'register-name-error' : null"
              />
            </div>
            @if (form.controls.name.invalid && form.controls.name.touched) {
              <p class="field-error" id="register-name-error">Name is required.</p>
            }
          </div>

          <div class="field">
            <label for="register-email">Email address</label>
            <div class="input-shell">
              <svg class="input-shell__icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M4 6h16v12H4zM4 7l8 6 8-6" />
              </svg>
              <input
                id="register-email"
                type="email"
                formControlName="email"
                autocomplete="email"
                placeholder="you@example.com"
                [attr.aria-invalid]="form.controls.email.invalid && form.controls.email.touched"
                [attr.aria-describedby]="form.controls.email.invalid && form.controls.email.touched ? 'register-email-error' : null"
              />
            </div>
            @if (form.controls.email.invalid && form.controls.email.touched) {
              <p class="field-error" id="register-email-error">Enter a valid email address.</p>
            }
          </div>

          <div class="field">
            <label for="register-password">Password</label>
            <div class="input-shell">
              <svg class="input-shell__icon" viewBox="0 0 24 24" aria-hidden="true">
                <rect x="5" y="10" width="14" height="10" rx="2" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v2" />
              </svg>
              <input
                id="register-password"
                type="password"
                formControlName="password"
                autocomplete="new-password"
                placeholder="At least 8 characters"
                [attr.aria-invalid]="form.controls.password.invalid && form.controls.password.touched"
                [attr.aria-describedby]="form.controls.password.invalid && form.controls.password.touched ? 'register-password-error' : 'register-password-hint'"
              />
            </div>
            @if (form.controls.password.invalid && form.controls.password.touched) {
              <p class="field-error" id="register-password-error">
                Use at least 8 characters.
              </p>
            } @else {
              <p class="field-hint" id="register-password-hint">Use 8 or more characters.</p>
            }
          </div>

          @if (error()) {
            <div class="notice notice--error" role="alert">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="12" r="9" />
                <path d="M12 7v6M12 17h.01" />
              </svg>
              <span>{{ error() }}</span>
            </div>
          }

          <button
            class="button button--primary button--wide"
            type="submit"
            [disabled]="form.invalid || loading()"
            [attr.aria-busy]="loading()"
          >
            <span>{{ loading() ? 'Creating…' : 'Create account' }}</span>
            @if (!loading()) {
              <svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M5 12h14M14 7l5 5-5 5" />
              </svg>
            }
          </button>
        </form>

        <p class="auth-panel__switch">
          Already have an account? <a routerLink="/login">Log in</a>
        </p>
      </div>
    </section>
  `,
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly registerUseCase = inject(RegisterUseCase);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.registerUseCase.execute(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/cars']),
      error: (err) => {
        this.error.set(err?.status === 409 ? 'Email already in use' : 'Could not create the account');
        this.loading.set(false);
      },
    });
  }
}
