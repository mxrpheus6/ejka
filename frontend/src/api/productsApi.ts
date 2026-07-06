import { apiClient } from "./client";
import type {
  ProductsResponse,
  Product,
  ProductRequest,
} from "../types/products";

interface GetProductsParams {
  offset: number;
  limit: number;
  sortBy?: string;
  sortDirection?: string;
  searchQuery?: string;
  category?: string;
  status?: string;
  minCalories?: number;
  maxCalories?: number;
  additiveIds?: string[];
}

export const productsApi = {
  getProducts: async (params: GetProductsParams): Promise<ProductsResponse> => {
    const response = await apiClient.get<ProductsResponse>("/products", {
      params,
    });
    return response.data;
  },

  getMyProducts: async (
    params: GetProductsParams,
  ): Promise<ProductsResponse> => {
    const response = await apiClient.get<ProductsResponse>("/products/me", {
      params,
    });
    return response.data;
  },

  getProductById: async (id: string): Promise<Product> => {
    const response = await apiClient.get<Product>(`/products/${id}`);
    return response.data;
  },

  getProductByBarcode: async (barcode: string): Promise<Product> => {
    const response = await apiClient.get<Product>("/products", {
      params: { barcode },
    });
    return response.data;
  },

  createProduct: async (
    data: ProductRequest,
    mainImage?: File | null,
    ingredientsImage?: File | null,
    barcodeImage?: File | null,
  ): Promise<Product> => {
    const formData = new FormData();

    formData.append(
      "data",
      new Blob([JSON.stringify(data)], { type: "application/json" }),
    );

    if (mainImage) formData.append("mainImage", mainImage);
    if (ingredientsImage) formData.append("ingredientsImage", ingredientsImage);
    if (barcodeImage) formData.append("barcodeImage", barcodeImage);

    const response = await apiClient.post<Product>("/products", formData);

    return response.data;
  },

  updateProduct: async (
    id: string,
    data: ProductRequest,
    mainImage?: File | null,
    ingredientsImage?: File | null,
    barcodeImage?: File | null,
    status?: string,
  ): Promise<Product> => {
    const formData = new FormData();
    formData.append(
      "data",
      new Blob([JSON.stringify(data)], { type: "application/json" }),
    );
    if (mainImage) formData.append("mainImage", mainImage);
    if (ingredientsImage) formData.append("ingredientsImage", ingredientsImage);
    if (barcodeImage) formData.append("barcodeImage", barcodeImage);
    if (status) formData.append("status", status);

    const response = await apiClient.put<Product>(`/products/${id}`, formData);
    return response.data;
  },
  deleteProduct: async (id: string): Promise<void> => {
    await apiClient.delete(`/products/${id}`);
  },
};
