import type { Additive } from "./additives";

export type ProductCategory =
  | "BEVERAGES"
  | "FATS_AND_OILS"
  | "NUTS_AND_SEEDS"
  | "CEREALS_AND_LEGUMES"
  | "MEAT_AND_FISH"
  | "SNACKS_AND_SWEETS"
  | "SAUCES"
  | "DAIRY"
  | "FRUITS_AND_VEGETABLES"
  | "GENERAL";

export const ProductCategoryLabels: Record<ProductCategory, string> = {
  BEVERAGES: "Напитки",
  FATS_AND_OILS: "Масла и масла",
  NUTS_AND_SEEDS: "Орехи и семена",
  CEREALS_AND_LEGUMES: "Злаки и бобовые",
  MEAT_AND_FISH: "Мясо и рыба",
  SNACKS_AND_SWEETS: "Снеки и сладости",
  SAUCES: "Соусы",
  DAIRY: "Молочные продукты",
  FRUITS_AND_VEGETABLES: "Фрукты и овощи",
  GENERAL: "Разное",
};

export type ModerationStatus =
  | "PENDING"
  | "APPROVED"
  | "REJECTED"
  | "NEEDS_INFO";

export const ModerationStatusLabels: Record<ModerationStatus, string> = {
  PENDING: "На проверке",
  APPROVED: "Одобрено",
  REJECTED: "Отклонено",
  NEEDS_INFO: "Не хватает информации",
};

export const ModerationStatusColors: Record<ModerationStatus, string> = {
  PENDING: "#f39c12",
  APPROVED: "#27ae60",
  REJECTED: "#e74c3c",
  NEEDS_INFO: "#3498db",
};

export type ImpactLevel = "EXCELLENT" | "GOOD" | "POOR" | "BAD";

export interface MacroDetail {
  name: string;
  value: string;
  numericValue?: number;
  thresholds?: number[];
  higherBetter?: boolean;
  score: number;
  impact: ImpactLevel;
}

export interface ScoreDetails {
  macros: MacroDetail[];
}

export interface ProductImage {
  objectKey: string;
  type: string;
}

export interface Product {
  id: string;
  barcode: string;
  title: string;
  category: ProductCategory;
  images: ProductImage[];
  nutritionScore?: number;
  scoreDetails?: ScoreDetails;
  userRating: number;
  reviewsCount: number;
  createdAt: string;
  moderationStatus: ModerationStatus;
  calories: number;
  proteins: number;
  fats: number;
  carbohydrates: number;
  additives?: Additive[];
  allergens?: string[];
  hasPalmOil?: boolean;
  compositionText: string;
  authorUsername?: string;
}

export interface ProductsResponse {
  values: Product[];
  totalPages: number;
  totalElements: number;
}

export interface ProductRequest {
  barcode: string;
  title: string;
  category: ProductCategory;
  calories: number;
  proteins: number;
  fats: number;
  carbohydrates: number;
  additiveIds: number[];
  allergens: string[];
  hasPalmOil: boolean;
  compositionText: string;
}
