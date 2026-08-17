import { AuthSession } from '../../domain/auth.model';

/** Outbound port for persisting the auth session. Implemented over localStorage. */
export abstract class SessionStoragePort {
  abstract save(session: AuthSession): void;
  abstract read(): AuthSession | null;
  abstract token(): string | null;
  abstract clear(): void;
}
