import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthRepositoryPort } from '../../application/ports/auth.repository.port';
import { AuthSession, Credentials, RegisterData } from '../../domain/auth.model';
import { API_BASE_URL } from './api.tokens';

@Injectable()
export class AuthHttpAdapter extends AuthRepositoryPort {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  override login(credentials: Credentials): Observable<AuthSession> {
    return this.http.post<AuthSession>(`${this.baseUrl}/api/auth/login`, credentials);
  }

  override register(data: RegisterData): Observable<AuthSession> {
    return this.http.post<AuthSession>(`${this.baseUrl}/api/auth/register`, data);
  }
}
