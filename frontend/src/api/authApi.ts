import { apiClient } from "./client";
import type {
  AuthResponse,
  UserResponse,
  LoginRequest,
  RegisterRequest,
} from "../types/auth";

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>("/auth/login", data);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>("/auth/register", data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await apiClient.post("/auth/logout");
  },

  getUserProfile: async (): Promise<UserResponse> => {
    const response = await apiClient.get<UserResponse>("/users/me");
    return response.data;
  },

  deleteUserProfile: async (): Promise<void> => {
    await apiClient.delete("/users/me");
  },

  uploadAvatar: async (file: File): Promise<void> => {
    const formData = new FormData();
    formData.append("file", file);

    await apiClient.post("/users/me/avatar", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  deleteAvatar: async (): Promise<void> => {
    await apiClient.delete("/users/me/avatar");
  },
};
