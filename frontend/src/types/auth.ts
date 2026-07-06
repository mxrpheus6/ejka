export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
  name: string;
  birthDate: string;
}

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  name: string;
  birthDate: string;
  registrationDate: string;
  role: string;
  isBanned: boolean;
  avatarKey: string | null;
  isPremium: boolean;
  cancelAtPeriodEnd: boolean;
  scansCount: number;
  premiumUntil: string | null;
}
