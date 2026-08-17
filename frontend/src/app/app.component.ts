import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import {
  CurrentSessionUseCase,
  LogoutUseCase,
} from './core/application/use-cases/auth.use-cases';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterLink, RouterOutlet],
  template: `
    <div class="app-shell">
      <header class="topbar">
        <a class="brand" routerLink="/cars" aria-label="Car Manager home">
          <span class="brand__mark" aria-hidden="true">
            <svg viewBox="0 0 24 24" role="img">
              <path d="M5.2 10.2 6.8 6h10.4l1.6 4.2M4 10.2h16v6.3H4zM7 16.5v1.7M17 16.5v1.7M7.2 13.2h.1M16.7 13.2h.1" />
            </svg>
          </span>
          <span>
            <span class="brand__name">Car Manager</span>
            <span class="brand__tagline">Personal garage</span>
          </span>
        </a>

        @if (session.isAuthenticated()) {
          <nav class="topbar__nav" aria-label="Account navigation">
            <div class="account-chip">
              <span class="account-chip__avatar" aria-hidden="true">
                {{ session.current()?.name?.charAt(0) }}
              </span>
              <span class="account-chip__copy">
                <span class="account-chip__label">Signed in as</span>
                <span class="account-chip__name">{{ session.current()?.name }}</span>
              </span>
            </div>
            <button type="button" class="button button--header" (click)="logout()">
              <svg class="button__icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M10 5H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h4M14 8l4 4-4 4M18 12H9" />
              </svg>
              <span>Log out</span>
            </button>
          </nav>
        }
      </header>

      <main class="content" id="main-content">
        <router-outlet />
      </main>
    </div>
  `,
})
export class AppComponent {
  protected readonly session = inject(CurrentSessionUseCase);
  private readonly logoutUseCase = inject(LogoutUseCase);
  private readonly router = inject(Router);

  protected logout(): void {
    this.logoutUseCase.execute();
    this.router.navigate(['/login']);
  }
}
