import { apiClient } from "./client";
import type {
  AdditivesResponse,
  Additive,
  AdditiveRequest,
  Origin,
  PageResponse,
} from "../types/additives";

export interface GetAdditivesParams {
  offset: number;
  limit: number;
  sortBy: string;
  sortDirection: string;
  category?: string;
  dangerLevel?: string;
  origin?: string[];
  searchQuery?: string;
}

export const additivesApi = {
  getAdditives: async (
    params: GetAdditivesParams,
  ): Promise<AdditivesResponse> => {
    const response = await apiClient.get<AdditivesResponse>("/additives", {
      params,
    });
    return response.data;
  },

  getAdditiveById: async (id: string): Promise<Additive> => {
    const response = await apiClient.get<Additive>(`/additives/${id}`);
    return response.data;
  },

  getAdditiveByCode: async (code: string): Promise<Additive> => {
    const response = await apiClient.get<Additive>(
      `/additives/code/${encodeURIComponent(code)}`,
    );
    return response.data;
  },

  getAdditivesBatch: async (ids: number[]): Promise<Additive[]> => {
    if (!ids || ids.length === 0) return [];

    const response = await apiClient.get<Additive[]>("/additives/batch", {
      params: { ids: ids.join(",") },
    });
    return response.data;
  },

  searchAdditives: async (searchQuery: string): Promise<Additive[]> => {
    if (!searchQuery) return [];
    const response = await apiClient.get<Additive[]>("/additives", {
      params: { searchQuery, limit: 5 },
    });
    return response.data;
  },
  createAdditive: async (data: AdditiveRequest): Promise<Additive> => {
    const response = await apiClient.post<Additive>("/additives", data);
    return response.data;
  },

  getOrigins: async (): Promise<Origin[]> => {
    const response = await apiClient.get<PageResponse<Origin>>("/origins");
    return response.data.values;
  },

  updateAdditive: async (
    id: string | number,
    data: AdditiveRequest,
  ): Promise<Additive> => {
    const response = await apiClient.put<Additive>(`/additives/${id}`, data);
    return response.data;
  },

  deleteAdditive: async (id: string | number): Promise<void> => {
    await apiClient.delete(`/additives/${id}`);
  },
};
