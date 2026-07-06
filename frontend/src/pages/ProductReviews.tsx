import React, { useEffect, useState } from "react";
import { Spinner, Button, Alert, Form, Pagination } from "react-bootstrap";
import { useSelector } from "react-redux";
import type { RootState } from "../store/store";
import { reviewsApi } from "../api/reviewsApi";
import type { Review } from "../types/reviews";
import styles from "../pages/ProductReviews.module.css";

interface ProductReviewsProps {
  productId: string;
  reviewsCount: number;
  setShowAuthModal: (show: boolean) => void;
  onReviewChanged: () => Promise<void>;
}

const ProductReviews: React.FC<ProductReviewsProps> = ({
  productId,
  reviewsCount,
  setShowAuthModal,
  onReviewChanged,
}) => {
  const { isAuthenticated, user } = useSelector(
    (state: RootState) => state.auth,
  );

  const [reviews, setReviews] = useState<Review[]>([]);
  const [reviewsPage, setReviewsPage] = useState<number>(1);
  const [reviewsTotalPages, setReviewsTotalPages] = useState<number>(1);
  const [reviewsLoading, setReviewsLoading] = useState<boolean>(false);
  const [userVotes, setUserVotes] = useState<Record<string, boolean>>({});

  const [newReviewContent, setNewReviewContent] = useState<string>("");
  const [newReviewRating, setNewReviewRating] = useState<number>(0);
  const [submittingReview, setSubmittingReview] = useState<boolean>(false);
  const [reviewError, setReviewError] = useState<string | null>(null);

  // Загрузка отзывов
  useEffect(() => {
    const fetchReviews = async () => {
      setReviewsLoading(true);
      try {
        const data = await reviewsApi.getReviewsByProductId(productId, {
          offset: reviewsPage - 1,
          limit: 5,
        });
        setReviews(data.values);
        setReviewsTotalPages(data.totalPages);
      } catch (error) {
        console.error(error);
      } finally {
        setReviewsLoading(false);
      }
    };

    fetchReviews();
  }, [productId, reviewsPage]);

  // Загрузка голосов пользователя
  useEffect(() => {
    const fetchMyVotes = async () => {
      if (isAuthenticated) {
        try {
          const votes = await reviewsApi.getMyVotes(productId);
          const votesMap: Record<string, boolean> = {};
          votes.forEach((v) => {
            votesMap[v.reviewId] = v.isUpvote;
          });
          setUserVotes(votesMap);
        } catch (error) {
          console.error("Не удалось загрузить голоса пользователя", error);
        }
      }
    };

    fetchMyVotes();
  }, [productId, isAuthenticated]);

  const handleReviewSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }
    if (newReviewRating === 0) {
      setReviewError("Пожалуйста, поставьте оценку от 1 до 5");
      return;
    }
    if (!newReviewContent.trim()) {
      setReviewError("Пожалуйста, напишите текст отзыва");
      return;
    }

    setSubmittingReview(true);
    setReviewError(null);
    try {
      await reviewsApi.createReview({
        productId,
        content: newReviewContent,
        rating: newReviewRating,
      });
      setNewReviewContent("");
      setNewReviewRating(0);

      // Обновляем список отзывов
      const data = await reviewsApi.getReviewsByProductId(productId, {
        offset: 0,
        limit: 5,
      });
      setReviews(data.values);
      setReviewsTotalPages(data.totalPages);
      setReviewsPage(1);

      // Оповещаем родителя об изменении (чтобы обновить общий рейтинг продукта)
      await onReviewChanged();
    } catch (error: any) {
      setReviewError(
        error.response?.data?.message ||
          "Не удалось отправить отзыв. Возможно, вы уже оставляли его.",
      );
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleDeleteReview = async (reviewId: string) => {
    try {
      await reviewsApi.deleteReview(reviewId);
      const data = await reviewsApi.getReviewsByProductId(productId, {
        offset: 0,
        limit: 5,
      });
      setReviews(data.values);
      setReviewsTotalPages(data.totalPages);
      setReviewsPage(1);

      // Оповещаем родителя
      await onReviewChanged();
    } catch (error) {
      console.error(error);
    }
  };

  const handleVote = async (reviewId: string, isUpvote: boolean) => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }
    try {
      await reviewsApi.voteForReview(reviewId, isUpvote);

      setUserVotes((prev) => {
        const newVotes = { ...prev };
        if (newVotes[reviewId] === isUpvote) {
          delete newVotes[reviewId];
        } else {
          newVotes[reviewId] = isUpvote;
        }
        return newVotes;
      });

      const data = await reviewsApi.getReviewsByProductId(productId, {
        offset: reviewsPage - 1,
        limit: 5,
      });
      setReviews(data.values);
    } catch (error) {
      console.error(error);
    }
  };

  const sortedReviews = [...reviews].sort((a, b) => {
    if (user && a.authorId === user.id) return -1;
    if (user && b.authorId === user.id) return 1;
    return 0;
  });

  const hasMyReview = reviews.some((r) => user && r.authorId === user.id);

  return (
    <div className="mt-5">
      <h4 className="mb-4">Отзывы ({reviewsCount})</h4>

      {(!isAuthenticated || !hasMyReview) && (
        <div className={styles.reviewFormContainer}>
          <h5>Оставить отзыв</h5>
          {reviewError && <Alert variant="danger">{reviewError}</Alert>}
          <Form onSubmit={handleReviewSubmit}>
            <div className={styles.starSelection}>
              {[1, 2, 3, 4, 5].map((star) => (
                <span
                  key={star}
                  className={`${styles.starSelect} ${star <= newReviewRating ? styles.starSelectActive : ""}`}
                  onClick={() => setNewReviewRating(star)}
                >
                  ★
                </span>
              ))}
            </div>
            <Form.Group className="mb-3">
              <Form.Control
                as="textarea"
                rows={3}
                placeholder="Поделитесь своим мнением о продукте..."
                value={newReviewContent}
                onChange={(e) => setNewReviewContent(e.target.value)}
              />
            </Form.Group>
            <Button variant="success" type="submit" disabled={submittingReview}>
              {submittingReview ? "Отправка..." : "Отправить отзыв"}
            </Button>
          </Form>
        </div>
      )}

      {reviewsLoading ? (
        <div className="text-center my-4">
          <Spinner animation="border" variant="success" />
        </div>
      ) : sortedReviews.length > 0 ? (
        <div className={styles.reviewsList}>
          {sortedReviews.map((review) => {
            const isMyReview = user && review.authorId === user.id;

            return (
              <div key={review.id} className={styles.reviewCard}>
                <div className={styles.reviewHeader}>
                  <div className={styles.reviewAuthorBox}>
                    <span className={styles.reviewAuthor}>
                      {review.username || "Удаленный пользователь"}
                    </span>
                    {isMyReview && (
                      <span className={styles.myReviewBadge}>Ваш отзыв</span>
                    )}
                    {review.createdAt && (
                      <span className={styles.reviewDate}>
                        {new Date(review.createdAt).toLocaleString("ru-RU", {
                          day: "numeric",
                          month: "long",
                          year: "numeric",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </span>
                    )}
                  </div>
                  <div className={styles.reviewStars}>
                    {[1, 2, 3, 4, 5].map((star) => (
                      <span
                        key={star}
                        className={
                          star <= review.rating
                            ? styles.starActive
                            : styles.starInactive
                        }
                      >
                        ★
                      </span>
                    ))}
                  </div>
                </div>
                <div className={styles.reviewContent}>{review.content}</div>
                <div className={styles.reviewActions}>
                  <span className={styles.usefulScoreText}>
                    Полезность: {review.usefulScore}
                  </span>
                  <div className={styles.actionButtonsRow}>
                    <div className={styles.voteButtons}>
                      <Button
                        variant={
                          userVotes[review.id] === true
                            ? "success"
                            : "outline-success"
                        }
                        size="sm"
                        onClick={() => handleVote(review.id, true)}
                      >
                        👍
                      </Button>
                      <Button
                        variant={
                          userVotes[review.id] === false
                            ? "danger"
                            : "outline-danger"
                        }
                        size="sm"
                        onClick={() => handleVote(review.id, false)}
                      >
                        👎
                      </Button>
                    </div>
                    {isMyReview && (
                      <Button
                        variant="outline-danger"
                        size="sm"
                        onClick={() => handleDeleteReview(review.id)}
                      >
                        Удалить
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            );
          })}

          {reviewsTotalPages > 1 && (
            <Pagination className="justify-content-center mt-4">
              <Pagination.Prev
                disabled={reviewsPage === 1}
                onClick={() => setReviewsPage((p) => p - 1)}
              />
              <Pagination.Item active>{reviewsPage}</Pagination.Item>
              <Pagination.Next
                disabled={reviewsPage === reviewsTotalPages}
                onClick={() => setReviewsPage((p) => p + 1)}
              />
            </Pagination>
          )}
        </div>
      ) : (
        <Alert variant="info">Отзывов пока нет. Будьте первым!</Alert>
      )}
    </div>
  );
};

export default ProductReviews;
