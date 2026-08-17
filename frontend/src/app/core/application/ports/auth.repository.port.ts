import { Observable } from 'rxjs';
import { AuthSession, Credentials, RegisterData } from '../../domain/auth.model';

/** Outbound port for authentication. Implemented by an HTTP adapter in infrastructure. */
export abstract class AuthRepositoryPort {
  abstract login(credentials: Credentials): Observable<AuthSession>;
  abstract register(data: RegisterData): Observable<AuthSession>;
}
