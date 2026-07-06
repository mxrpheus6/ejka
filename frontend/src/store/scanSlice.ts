import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";
import type { ScanResponse } from "../types/scan";
import type { Additive } from "../types/additives";

interface ScanState {
  previewUrl: string | null;
  scanResult: ScanResponse | null;
  detailedAdditives: Additive[];
}

const initialState: ScanState = {
  previewUrl: null,
  scanResult: null,
  detailedAdditives: [],
};

const scanSlice = createSlice({
  name: "scan",
  initialState,
  reducers: {
    setPreviewUrl: (state, action: PayloadAction<string>) => {
      state.previewUrl = action.payload;
      state.scanResult = null;
      state.detailedAdditives = [];
    },
    setScanResult: (state, action: PayloadAction<ScanResponse>) => {
      state.scanResult = action.payload;
    },
    setDetailedAdditives: (state, action: PayloadAction<Additive[]>) => {
      state.detailedAdditives = action.payload;
    },
    clearScan: (state) => {
      state.previewUrl = null;
      state.scanResult = null;
      state.detailedAdditives = [];
    },
  },
});

export const { setPreviewUrl, setScanResult, setDetailedAdditives, clearScan } =
  scanSlice.actions;
export default scanSlice.reducer;
