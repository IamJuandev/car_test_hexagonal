import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LoginUseCase } from '../../../core/application/use-cases/auth.use-cases';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-layout" aria-labelledby="login-title">
      <aside class="auth-story" aria-label="Car Manager overview">
        <span class="eyebrow eyebrow--light">Your garage, organized</span>
        <h1>Every car detail,<br />ready when you need it.</h1>
        <p>
          Keep your vehicles, registration details, and essential information in one
          focused workspace.
        </p>
        <ul class="auth-story__benefits" aria-label="Product benefits">
          <li>
            <span class="benefit-icon" aria-hidden="true">01</span>
            A clear view of your complete garage
          </li>
          <li>
            <span class="benefit-icon" aria-hidden="true">02</span>
            Fast search by vehicle or plate
          </li>
          <li>
            <span class="benefit-icon" aria-hidden="true">03</span>
            Secure access to your personal records
          </li>
        </ul>
      </aside>

      <div class="auth-panel">
        <div class="auth-panel__heading">
          <span class="eyebrow">Welcome back</span>
          <h2 id="login-title">Log in to your garage</h2>
          <p>Enter the email and password associated with your account.</p>
        </div>

        <form class="auth-form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <div class="field">
            <label for="login-email">Email address</label>
            <div class="input-shell">
              <svg class="input-shell__icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M4 6h16v12H4zM4 7l8 6 8-6" />
              </svg>
              <input
                id="login-email"
                type="email"
                formControlName="email"
                autocomplete="email"
                placeholder="you@example.com"
                [attr.aria-invalid]="form.controls.email.invalid && form.controls.email.touched"
                [attr.aria-describedby]="form.controls.email.invalid && form.controls.email.touched ? 'login-email-error' : null"
              />
            </div>
            @if (form.controls.email.invalid && form.controls.email.touched) {
              <p class="field-error" id="login-email-error">
                Enter a valid email address.
              </p>
            }
          </div>

          <div class="field">
            <label for="login-password">Password</label>
            <div class="input-shell">
              <svg class="input-shell__icon" viewBox="0 0 24 24" aria-hidden="true">
                <rect x="5" y="10" width="14" height="10" rx="2" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v2" />
              </svg>
              <input
                id="login-password"
                type="password"
                formControlName="password"
                autocomplete="current-password"
                placeholder="Your password"
                [attr.aria-invalid]="form.controls.password.invalid && form.controls.password.touched"
                [attr.aria-describedby]="form.controls.password.invalid && form.controls.password.touched ? 'login-password-error' : null"
              />
            </div>
            @if (form.controls.password.invalid && form.controls.password.touched) {
              <p class="field-error" id="login-password-error">Password is required.</p>
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
            <span>{{ loading() ? 'Logging in…' : 'Log in' }}</span>
            @if (!loading()) {
              <svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M5 12h14M14 7l5 5-5 5" />
              </svg>
            }
          </button>
        </form>

        <p class="auth-panel__switch">
          New to Car Manager? <a routerLink="/register">Create an account</a>
        </p>
      </div>
    </section>
  `,
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly loginUseCase = inject(LoginUseCase);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.loginUseCase.execute(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/cars']),
      error: () => {
        this.error.set('Invalid email or password');
        this.loading.set(false);
      },
    });
  }
}
