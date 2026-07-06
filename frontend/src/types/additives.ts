export type DangerLevel = "SAFE" | "WARNING" | "DANGEROUS" | "BANNED";

export interface AdditiveRequest {
  code: string;
  nameRu?: string;
  nameEn?: string;
  category?: string;
  dangerLevel?: DangerLevel;
  warningDescription?: string;
  description?: string;
  originIds: number[];
}

export interface Origin {
  id: number;
  type: string;
}

export interface Additive {
  id: number;
  code: string;
  nameRu: string;
  nameEn: string;
  category: string;
  dangerLevel: DangerLevel;
  warningDescription: string | null;
  description: string;
  origins: Origin[];
}

export interface AdditivesResponse {
  currentOffset: number;
  currentLimit: number;
  totalPages: number;
  totalElements: number;
  sort: string;
  values: Additive[];
}

export interface PageResponse<T> {
  currentOffset: number;
  currentLimit: number;
  totalPages: number;
  totalElements: number;
  sort: string;
  values: T[];
}
