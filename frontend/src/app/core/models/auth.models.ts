export type UserRole = 'ADMIN' | 'HOSPITAL';

export interface AuthUser {
  id: number;
  username: string;
  role: UserRole;
  organizationSyntheaId: string | null;
  hospitalName: string | null;
  admin: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  user: AuthUser;
}
