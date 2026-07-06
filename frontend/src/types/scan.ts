export interface ScanAdditive {
  raw: string;
  id: number;
  code: string;
  nameRu: string;
  score: number;
}

export interface ScanIngredientMatch {
  category: string;
  matchedText: string;
  score: number;
  id?: number;
}

export interface ScanResponse {
  id: string;
  objectKey: string;
  status: string;
  parsedText: string;
  additives: ScanAdditive[];
  allergens?: ScanIngredientMatch[];
  controversial?: ScanIngredientMatch[];
  errorMessage: string | null;
}
