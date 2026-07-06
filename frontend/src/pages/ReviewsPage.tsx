import React, { useEffect, useState } from "react";
import {
  Container,
  Spinner,
  Button,
  Alert,
  Row,
  Col,
  Carousel,
  Modal,
  Form,
  Pagination,
} from "react-bootstrap";
import { useParams, useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import type { RootState } from "../store/store";
import { productsApi } from "../api/productsApi";
import { reviewsApi } from "../api/reviewsApi";
import type { Product } from "../types/products";
import type { DangerLevel } from "../types/additives";
import type { Review } from "../types/reviews";
import { originTranslations } from "./AdditivesPage";
import styles from "./ProductDetailsPage.module.css";

const MINIO_BASE_URL = import.meta.env.VITE_MINIO_PRODUCTS_URL || "";
const DANGER_LEVELS: DangerLevel[] = ["SAFE", "WARNING", "DANGEROUS", "BANNED"];

const ProductDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);

  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const [reviews, setReviews] = useState<Review[]>([]);
  const [reviewsPage, setReviewsPage] = useState<number>(1);
  const [reviewsTotalPages, setReviewsTotalPages] = useState<number>(1);
  const [reviewsLoading, setReviewsLoading] = useState<boolean>(false);

  const [showAuthModal, setShowAuthModal] = useState<boolean>(false);
  const [newReviewContent, setNewReviewContent] = useState<string>("");
  const [newReviewRating, setNewReviewRating] = useState<number>(0);
  const [submittingReview, setSubmittingReview] = useState<boolean>(false);
  const [reviewError, setReviewError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        const data = await productsApi.getProductById(id as string);
        setProduct(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    if (id) fetchDetails();
  }, [id]);

  useEffect(() => {
    const fetchReviews = async () => {
      if (!id) return;
      setReviewsLoading(true);
      try {
        const data = await reviewsApi.getReviewsByProductId(id, {
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
  }, [id, reviewsPage]);

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
        productId: id as string,
        content: newReviewContent,
        rating: newReviewRating,
      });
      setNewReviewContent("");
      setNewReviewRating(0);
      const data = await reviewsApi.getReviewsByProductId(id as string, {
        offset: 0,
        limit: 5,
      });
      setReviews(data.values);
      setReviewsTotalPages(data.totalPages);
      setReviewsPage(1);

      const updatedProduct = await productsApi.getProductById(id as string);
      setProduct(updatedProduct);
    } catch (error: any) {
      setReviewError(
        error.response?.data?.message ||
          "Не удалось отправить отзыв. Возможно, вы уже оставляли его.",
      );
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleVote = async (reviewId: string, isUpvote: boolean) => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }
    try {
      await reviewsApi.voteForReview(reviewId, isUpvote);
      setReviews((prev) =>
        prev.map((r) => {
          if (r.id === reviewId) {
            return {
              ...r,
              usefulScore: isUpvote ? r.usefulScore + 1 : r.usefulScore - 1,
            };
          }
          return r;
        }),
      );
    } catch (error) {
      console.error(error);
    }
  };

  if (loading) {
    return (
      <Container className="mt-5 text-center">
        <Spinner animation="border" variant="success" />
      </Container>
    );
  }

  if (!product) {
    return (
      <Container className="mt-5 text-center">
        <Alert variant="danger">Продукт не найден</Alert>
        <Button variant="outline-secondary" onClick={() => navigate(-1)}>
          Назад
        </Button>
      </Container>
    );
  }

  return (
    <Container className="mt-4 pb-5">
      <Button
        variant="outline-secondary"
        className="mb-4"
        onClick={() => navigate(-1)}
      >
        &larr; Назад к каталогу
      </Button>

      <div className={styles.detailsCard}>
        <Row>
          <Col md={4} className="mb-4 mb-md-0">
            <div className={styles.imageContainer}>
              {(() => {
                const images = product.images || [];

                if (images.length === 0) {
                  return <div className={styles.noImage}>Нет фото</div>;
                }

                const sortedImages = [...images].sort((a, b) => {
                  if (a.type === "MAIN") return -1;
                  if (b.type === "MAIN") return 1;
                  return 0;
                });

                if (sortedImages.length === 1) {
                  return (
                    <img
                      src={`${MINIO_BASE_URL}${sortedImages[0].objectKey}`}
                      alt={product.title}
                      className={styles.mainImage}
                    />
                  );
                }

                return (
                  <Carousel
                    className={styles.carousel}
                    interval={null}
                    variant="dark"
                  >
                    {sortedImages.map((img, idx) => (
                      <Carousel.Item key={idx} className={styles.carouselItem}>
                        <div className={styles.slideContent}>
                          <img
                            src={`${MINIO_BASE_URL}${img.objectKey}`}
                            alt={`${product.title} - ${idx + 1}`}
                            className={styles.mainImage}
                          />
                        </div>
                      </Carousel.Item>
                    ))}
                  </Carousel>
                );
              })()}
            </div>
          </Col>
          <Col md={8}>
            <div className={styles.header}>
              <div className={styles.category}>{product.category}</div>
              <div className={styles.barcode}>Штрихкод: {product.barcode}</div>
            </div>

            <h2 className={styles.title}>{product.title}</h2>

            <div className={styles.ratingRow}>
              <span className={styles.userRating}>
                ★ {product.userRating ? product.userRating.toFixed(1) : "—"}
              </span>
              <span className={styles.ratingSystem}>
                Оценка системы: {product.userRating}
              </span>
            </div>

            <div className={styles.macrosBox}>
              <div className={styles.macroItem}>
                <span className={styles.macroValue}>{product.calories}</span>
                <span className={styles.macroLabel}>Ккал</span>
              </div>
              <div className={styles.macroItem}>
                <span className={styles.macroValue}>{product.proteins} г</span>
                <span className={styles.macroLabel}>Белки</span>
              </div>
              <div className={styles.macroItem}>
                <span className={styles.macroValue}>{product.fats} г</span>
                <span className={styles.macroLabel}>Жиры</span>
              </div>
              <div className={styles.macroItem}>
                <span className={styles.macroValue}>
                  {product.carbohydrates} г
                </span>
                <span className={styles.macroLabel}>Углеводы</span>
              </div>
            </div>
          </Col>
        </Row>
      </div>

      {product.additives && product.additives.length > 0 && (
        <div className="mt-5">
          <h4 className="mb-4">Состав добавок ({product.additives.length})</h4>
          <Row className="g-4 align-items-stretch">
            {product.additives.map((additive) => (
              <Col key={additive.id} xs={12} sm={6} md={4}>
                <div
                  className={styles.additiveCard}
                  onClick={() => navigate(`/additives/${additive.id}`)}
                >
                  <div className={styles.additiveTopSection}>
                    <div className={styles.additiveCode}>
                      {additive.code || "Код неизвестен"}
                    </div>
                    <div className={styles.additiveName}>
                      {additive.nameRu || "Название не указано"}
                    </div>
                    <div className={styles.additiveCategory}>
                      {additive.category || "Категория не указана"}
                    </div>
                  </div>

                  <div className={styles.additiveBottomSection}>
                    <div className={styles.originsList}>
                      {additive.origins && additive.origins.length > 0 ? (
                        additive.origins.map((o) => (
                          <span
                            key={o.id}
                            className={`${styles.originBadge} ${styles[`bgOrigin${o.type}`] || styles.bgOriginDEFAULT}`}
                          >
                            {originTranslations[o.type] || o.type}
                          </span>
                        ))
                      ) : (
                        <span
                          className={`${styles.originBadge} ${styles.bgOriginDEFAULT}`}
                        >
                          Не указано
                        </span>
                      )}
                    </div>

                    <div className={styles.dangerIndicator}>
                      {DANGER_LEVELS.map((level) => (
                        <div
                          key={level}
                          className={`${styles.rect} ${styles[`bg${level}`]} ${additive.dangerLevel === level ? styles.rectActive : ""}`}
                        />
                      ))}
                    </div>
                  </div>
                </div>
              </Col>
            ))}
          </Row>
        </div>
      )}

      <div className="mt-5">
        <h4 className="mb-4">Отзывы ({product.reviewsCount})</h4>

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

        {reviewsLoading ? (
          <div className="text-center my-4">
            <Spinner animation="border" variant="success" />
          </div>
        ) : reviews.length > 0 ? (
          <div className={styles.reviewsList}>
            {reviews.map((review) => (
              <div key={review.id} className={styles.reviewCard}>
                <div className={styles.reviewHeader}>
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
                  <div className={styles.voteButtons}>
                    <Button
                      variant="outline-success"
                      size="sm"
                      onClick={() => handleVote(review.id, true)}
                    >
                      👍
                    </Button>
                    <Button
                      variant="outline-danger"
                      size="sm"
                      onClick={() => handleVote(review.id, false)}
                    >
                      👎
                    </Button>
                  </div>
                </div>
              </div>
            ))}

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

      <Modal
        show={showAuthModal}
        onHide={() => setShowAuthModal(false)}
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title>Требуется авторизация</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Эта функция доступна только авторизованным пользователям. Пожалуйста,
          войдите в свой аккаунт.
        </Modal.Body>
        <Modal.Footer>
          <Button
            variant="outline-secondary"
            onClick={() => setShowAuthModal(false)}
          >
            Отмена
          </Button>
          <Button
            variant="success"
            style={{ backgroundColor: "#539155", borderColor: "#539155" }}
            onClick={() => navigate("/profile")}
          >
            Авторизоваться
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default ProductDetailsPage;
