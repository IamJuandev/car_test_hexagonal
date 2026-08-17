import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthRepositoryPort } from '../ports/auth.repository.port';
import { SessionStoragePort } from '../ports/session-storage.port';
import { AuthSession, Credentials, RegisterData } from '../../domain/auth.model';

/**
 * Authentication use cases. On success they persist the session so the rest of
 * the app (and the JWT interceptor) can read the token.
 */
@Injectable({ providedIn: 'root' })
export class LoginUseCase {
  private readonly auth = inject(AuthRepositoryPort);
  private readonly session = inject(SessionStoragePort);

  execute(credentials: Credentials): Observable<AuthSession> {
    return this.auth.login(credentials).pipe(tap((s) => this.session.save(s)));
  }
}

@Injectable({ providedIn: 'root' })
export class RegisterUseCase {
  private readonly auth = inject(AuthRepositoryPort);
  private readonly session = inject(SessionStoragePort);

  execute(data: RegisterData): Observable<AuthSession> {
    return this.auth.register(data).pipe(tap((s) => this.session.save(s)));
  }
}

@Injectable({ providedIn: 'root' })
export class LogoutUseCase {
  private readonly session = inject(SessionStoragePort);

  execute(): void {
    this.session.clear();
  }
}

@Injectable({ providedIn: 'root' })
export class CurrentSessionUseCase {
  private readonly session = inject(SessionStoragePort);

  isAuthenticated(): boolean {
    return this.session.token() !== null;
  }

  current(): AuthSession | null {
    return this.session.read();
  }
}
