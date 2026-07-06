export interface Review {
  id: string;
  productId: string;
  authorId: string;
  username: string;
  content: string;
  rating: number;
  usefulScore: number;
  createdAt: string;
}

export interface ReviewsResponse {
  values: Review[];
  totalPages: number;
  totalElements: number;
}

export interface UserVote {
  reviewId: string;
  isUpvote: boolean;
}
