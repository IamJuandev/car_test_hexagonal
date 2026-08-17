import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { CurrentSessionUseCase } from '../../core/application/use-cases/auth.use-cases';
import { guestGuard } from './auth.guard';

describe('guestGuard', () => {
  const session = jasmine.createSpyObj<CurrentSessionUseCase>('CurrentSessionUseCase', [
    'isAuthenticated',
  ]);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: CurrentSessionUseCase, useValue: session },
      ],
    });
  });

  function runGuard(): boolean | UrlTree {
    return TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never)) as
      | boolean
      | UrlTree;
  }

  it('redirects authenticated users to the cars page', () => {
    session.isAuthenticated.and.returnValue(true);

    const result = runGuard();

    expect(result instanceof UrlTree).toBeTrue();
    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/cars');
  });

  it('allows unauthenticated users to continue', () => {
    session.isAuthenticated.and.returnValue(false);

    expect(runGuard()).toBeTrue();
  });
});
