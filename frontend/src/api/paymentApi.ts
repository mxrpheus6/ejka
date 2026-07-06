import { apiClient } from "./client";
import type { PlanResponse } from "../types/payments";

export const paymentApi = {
  createCheckoutSession: async (): Promise<string> => {
    const response = await apiClient.post<{ url: string }>(
      "/subscriptions/create-checkout-session",
    );
    return response.data.url;
  },

  cancelSubscription: async (): Promise<void> => {
    await apiClient.post("/subscriptions/cancel");
  },

  getPlanDetails: async (): Promise<PlanResponse> => {
    const response = await apiClient.get<PlanResponse>("/subscriptions/plan");
    return response.data;
  },
};
