import { apiClient } from "./client";
import type { ScanResponse } from "../types/scan";

export const scansApi = {
  analyze: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await apiClient.post<{ scanId: string }>(
      "/scans/analyze",
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      },
    );
    return response.data.scanId;
  },

  getResult: async (scanId: string) => {
    return await apiClient.get<ScanResponse | "">(`/scans/result/${scanId}`);
  },
};
