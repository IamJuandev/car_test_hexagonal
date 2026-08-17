export interface Credentials {
  email: string;
  password: string;
}

export interface RegisterData extends Credentials {
  name: string;
}

export interface AuthSession {
  token: string;
  userId: number;
  name: string;
  email: string;
}
