import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";
import type { ProductsResponse } from "../types/products";

interface ProductsState {
  cache: Record<number, ProductsResponse>;
}

const initialState: ProductsState = {
  cache: {},
};

const productsSlice = createSlice({
  name: "products",
  initialState,
  reducers: {
    saveProductsPage: (
      state,
      action: PayloadAction<{ page: number; data: ProductsResponse }>,
    ) => {
      state.cache[action.payload.page] = action.payload.data;
    },
  },
});

export const { saveProductsPage } = productsSlice.actions;
export default productsSlice.reducer;
