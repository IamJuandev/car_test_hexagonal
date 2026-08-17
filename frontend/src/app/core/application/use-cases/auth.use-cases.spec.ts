import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { LoginUseCase } from './auth.use-cases';
import { AuthRepositoryPort } from '../ports/auth.repository.port';
import { SessionStoragePort } from '../ports/session-storage.port';
import { AuthSession } from '../../domain/auth.model';

describe('LoginUseCase', () => {
  const session: AuthSession = { token: 'JWT', userId: 1, name: 'Ana', email: 'ana@test.com' };

  it('logs in and persists the session via the storage port', (done) => {
    const authPort = { login: jasmine.createSpy('login').and.returnValue(of(session)), register: jasmine.createSpy() };
    const storage = jasmine.createSpyObj<SessionStoragePort>('SessionStoragePort', ['save', 'read', 'token', 'clear']);

    TestBed.configureTestingModule({
      providers: [
        LoginUseCase,
        { provide: AuthRepositoryPort, useValue: authPort },
        { provide: SessionStoragePort, useValue: storage },
      ],
    });

    TestBed.inject(LoginUseCase)
      .execute({ email: 'ana@test.com', password: 'password123' })
      .subscribe((result) => {
        expect(result).toEqual(session);
        expect(authPort.login).toHaveBeenCalled();
        expect(storage.save).toHaveBeenCalledWith(session);
        done();
      });
  });
});
