import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";
import type { AdditivesResponse } from "../types/additives";

interface AdditivesState {
  cache: Record<number, AdditivesResponse>;
}

const initialState: AdditivesState = {
  cache: {},
};

const additivesSlice = createSlice({
  name: "additives",
  initialState,
  reducers: {
    saveAdditivesPage: (
      state,
      action: PayloadAction<{ page: number; data: AdditivesResponse }>,
    ) => {
      state.cache[action.payload.page] = action.payload.data;
    },
    clearAdditivesCache: (state) => {
      state.cache = {};
    },
  },
});

export const { saveAdditivesPage, clearAdditivesCache } =
  additivesSlice.actions;
export default additivesSlice.reducer;
