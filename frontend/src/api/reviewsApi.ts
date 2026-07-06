import { apiClient } from "./client";
import type { Review, ReviewsResponse, UserVote } from "../types/reviews";

export const reviewsApi = {
  getMyVotes: async (productId: string): Promise<UserVote[]> => {
    const response = await apiClient.get<UserVote[]>("/reviews/my-votes", {
      params: { productId },
    });
    return response.data;
  },
  getReviewsByProductId: async (
    productId: string,
    params: {
      offset: number;
      limit: number;
      sortBy?: string;
      sortDirection?: string;
    },
  ): Promise<ReviewsResponse> => {
    const response = await apiClient.get<ReviewsResponse>("/reviews", {
      params: { productId, ...params },
    });
    return response.data;
  },

  createReview: async (data: {
    productId: string;
    content: string;
    rating: number;
  }): Promise<Review> => {
    const response = await apiClient.post<Review>("/reviews", data);
    return response.data;
  },

  deleteReview: async (reviewId: string): Promise<void> => {
    await apiClient.delete(`/reviews/${reviewId}`);
  },

  voteForReview: async (reviewId: string, isUpvote: boolean): Promise<void> => {
    await apiClient.post(`/reviews/${reviewId}/vote`, { isUpvote });
  },
};
