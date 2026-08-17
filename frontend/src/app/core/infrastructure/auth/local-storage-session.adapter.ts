import { Injectable } from '@angular/core';
import { SessionStoragePort } from '../../application/ports/session-storage.port';
import { AuthSession } from '../../domain/auth.model';

@Injectable()
export class LocalStorageSessionAdapter extends SessionStoragePort {
  private static readonly KEY = 'car-app.session';

  override save(session: AuthSession): void {
    localStorage.setItem(LocalStorageSessionAdapter.KEY, JSON.stringify(session));
  }

  override read(): AuthSession | null {
    const value = localStorage.getItem(LocalStorageSessionAdapter.KEY);
    if (!value) {
      return null;
    }
    try {
      return JSON.parse(value) as AuthSession;
    } catch {
      return null;
    }
  }

  override token(): string | null {
    return this.read()?.token ?? null;
  }

  override clear(): void {
    localStorage.removeItem(LocalStorageSessionAdapter.KEY);
  }
}
