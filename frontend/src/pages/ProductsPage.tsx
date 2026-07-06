import React, { useEffect, useState } from "react";
import {
  Container,
  Row,
  Col,
  Spinner,
  Pagination,
  Badge,
  Button,
  ButtonGroup,
  ToggleButton,
} from "react-bootstrap";
import { useSearchParams, useNavigate, useLocation } from "react-router-dom";
import { useSelector } from "react-redux";
import type { RootState } from "../store/store";
import { productsApi } from "../api/productsApi";
import type { ProductsResponse } from "../types/products";
import styles from "./ProductsPage.module.css";
import CategoryBadge from "../components/CategoryBadge";
import ProductsFilter from "../components/ProductsFilter";
import { useAuthRole } from "../hooks/useAuthRole";

const MINIO_BASE_URL = import.meta.env.VITE_MINIO_PRODUCTS_URL || "";

const COLORS = {
  EXCELLENT: "#10b981",
  GOOD: "#34d399",
  POOR: "#fbbf24",
  BAD: "#ef4444",
};

const getScoreColor = (score?: number | null) => {
  if (score === undefined || score === null) return "#cbd5e1";
  if (score >= 75) return COLORS.EXCELLENT;
  if (score >= 50) return COLORS.GOOD;
  if (score >= 25) return COLORS.POOR;
  return COLORS.BAD;
};

const STATUS_BADGES: Record<string, { label: string; bg: string }> = {
  PENDING: { label: "На проверке", bg: "warning" },
  APPROVED: { label: "Проверен", bg: "success" },
  REJECTED: { label: "Отклонен", bg: "danger" },
};

const ProductsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const location = useLocation();

  const isAuthenticated = useSelector(
    (state: RootState) => state.auth.isAuthenticated,
  );

  const { isModerator, role } = useAuthRole();
  const canEdit = isModerator || role === "ROLE_ADMIN";

  const viewMode = location.pathname.includes("/products/me") ? "my" : "all";

  const currentPageStr = searchParams.get("page");
  const currentPage = currentPageStr ? parseInt(currentPageStr, 10) : 1;
  const offset = Math.max(0, currentPage - 1);

  const searchQuery = searchParams.get("searchQuery") || "";
  const category = searchParams.get("category") || "";
  const status = searchParams.get("status") || "";
  const minCalories = searchParams.get("minCalories") || "";
  const maxCalories = searchParams.get("maxCalories") || "";
  const minUserRating = searchParams.get("minUserRating") || "";

  const [data, setData] = useState<ProductsResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const limit = 12;
  const sortBy = searchParams.get("sortBy") || "createdAt";
  const sortDirection = searchParams.get("sortDirection") || "desc";

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      setError(null);
      try {
        const params = {
          offset,
          limit,
          sortBy,
          sortDirection,
          searchQuery: searchQuery || undefined,
          category: category || undefined,
          status: status || undefined,
          minCalories: minCalories ? parseInt(minCalories, 10) : undefined,
          maxCalories: maxCalories ? parseInt(maxCalories, 10) : undefined,
          minUserRating: minUserRating ? parseFloat(minUserRating) : undefined,
        };

        let responseData;
        if (viewMode === "my" && isAuthenticated) {
          responseData = await productsApi.getMyProducts(params);
        } else {
          responseData = await productsApi.getProducts(params);
        }

        setData(responseData);
      } catch (err: any) {
        console.error("Ошибка при загрузке продуктов:", err);
        const backendMessage =
          err.response?.data?.message ||
          "Произошла ошибка при загрузке каталога.";
        setError(backendMessage);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [
    currentPage,
    offset,
    limit,
    sortBy,
    sortDirection,
    searchQuery,
    category,
    status,
    minCalories,
    maxCalories,
    minUserRating,
    viewMode,
    isAuthenticated,
  ]);

  const handlePageChange = (pageNumber: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set("page", pageNumber.toString());
    setSearchParams(newParams);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleCardClick = (id: string) => {
    navigate(`/products/${id}`);
  };

  const handleViewChange = (mode: "all" | "my") => {
    if (mode === viewMode) return;
    if (mode === "my") {
      navigate("/products/me");
    } else {
      navigate("/products");
    }
  };

  const clearStatusFilter = () => {
    const newParams = new URLSearchParams(searchParams);
    newParams.delete("status");
    newParams.set("page", "1");
    setSearchParams(newParams);
  };

  const renderPagination = () => {
    if (!data || data.totalPages <= 1 || error) return null;
    const { totalPages } = data;
    let items = [];

    items.push(
      <Pagination.Prev
        key="prev"
        disabled={currentPage === 1}
        onClick={() => handlePageChange(currentPage - 1)}
      />,
    );

    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, currentPage + 2);

    if (currentPage <= 3) {
      endPage = Math.min(totalPages, 5);
    } else if (currentPage >= totalPages - 2) {
      startPage = Math.max(1, totalPages - 4);
    }

    for (let number = startPage; number <= endPage; number++) {
      items.push(
        <Pagination.Item
          key={number}
          active={number === currentPage}
          onClick={() => handlePageChange(number)}
        >
          {number}
        </Pagination.Item>,
      );
    }

    items.push(
      <Pagination.Next
        key="next"
        disabled={currentPage === totalPages}
        onClick={() => handlePageChange(currentPage + 1)}
      />,
    );

    return (
      <Pagination className="justify-content-center mt-4 flex-wrap gap-1">
        {items}
      </Pagination>
    );
  };

  return (
    <Container className="mt-4 pb-5">
      <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <div className="d-flex align-items-center gap-4 flex-wrap">
          <h2 className="mb-0">Каталог продуктов</h2>

          {isAuthenticated && (
            <ButtonGroup>
              <ToggleButton
                id="toggle-all"
                type="radio"
                variant={viewMode === "all" ? "success" : "outline-success"}
                name="viewMode"
                value="all"
                checked={viewMode === "all"}
                onChange={() => handleViewChange("all")}
              >
                Все
              </ToggleButton>
              <ToggleButton
                id="toggle-my"
                type="radio"
                variant={viewMode === "my" ? "success" : "outline-success"}
                name="viewMode"
                value="my"
                checked={viewMode === "my"}
                onChange={() => handleViewChange("my")}
              >
                Мои продукты
              </ToggleButton>
            </ButtonGroup>
          )}
        </div>

        {isAuthenticated && (
          <Button
            variant="success"
            className="fw-bold rounded-pill shadow-sm px-4"
            onClick={() => navigate("/products/create")}
          >
            + Добавить продукт
          </Button>
        )}
      </div>

      <ProductsFilter showStatus={canEdit || viewMode === "my"} />

      {loading && !data && !error ? (
        <div className="text-center mt-5">
          <Spinner animation="border" variant="success" />
        </div>
      ) : error ? (
        <div className="text-center mt-5">
          <h4 className="text-danger mb-3">⛔ Ошибка доступа</h4>
          <p className="text-muted fs-5">{error}</p>
          <Button
            variant="outline-danger"
            className="mt-3"
            onClick={clearStatusFilter}
          >
            Сбросить фильтр и вернуться
          </Button>
        </div>
      ) : (
        <>
          {data?.values.length === 0 ? (
            <div className="text-center text-muted mt-5">
              <h4>Ничего не найдено 😔</h4>
              <p>
                {viewMode === "my"
                  ? "Вы еще не добавили ни одного продукта."
                  : "Попробуйте изменить параметры фильтрации или поисковой запрос."}
              </p>
            </div>
          ) : (
            <Row className="g-4 mb-4 align-items-stretch">
              {data?.values.map((product) => {
                const displayImage =
                  product.images?.find((img) => img.type === "MAIN") ||
                  product.images?.[0];

                return (
                  <Col key={product.id} xs={12} sm={6} md={4} lg={3}>
                    <div
                      className={styles.card}
                      onClick={() => handleCardClick(product.id)}
                    >
                      <div className={styles.imageWrapper}>
                        {product.nutritionScore !== undefined &&
                          product.nutritionScore !== null && (
                            <div
                              className={styles.scoreBadge}
                              style={{
                                backgroundColor: getScoreColor(
                                  product.nutritionScore,
                                ),
                              }}
                            >
                              <span className={styles.scoreValue}>
                                {product.nutritionScore}
                              </span>
                              <span className={styles.scoreMax}>/100</span>
                            </div>
                          )}

                        {canEdit && (
                          <Button
                            variant="light"
                            size="sm"
                            title="Редактировать продукт"
                            className="position-absolute shadow-sm d-flex align-items-center justify-content-center"
                            style={{
                              top: "10px",
                              left: "10px",
                              zIndex: 10,
                              width: "32px",
                              height: "32px",
                              borderRadius: "50%",
                              padding: 0,
                            }}
                            onClick={(e) => {
                              e.stopPropagation();
                              navigate(`/products/${product.id}/edit`);
                            }}
                          >
                            ✏️
                          </Button>
                        )}

                        {displayImage ? (
                          <img
                            src={`${MINIO_BASE_URL}${displayImage.objectKey}`}
                            alt={product.title}
                            className={styles.image}
                          />
                        ) : (
                          <div
                            className={`${styles.noImage} d-flex flex-column align-items-center justify-content-center bg-light text-muted w-100`}
                            style={{ minHeight: "180px", height: "100%" }}
                          >
                            <span style={{ fontSize: "2rem" }}>📷</span>
                            <span className="small mt-2">Нет фото</span>
                          </div>
                        )}
                      </div>

                      <div className={styles.contentSection}>
                        <div className={styles.title}>{product.title}</div>

                        {product.moderationStatus && (
                          <div className="mb-2">
                            <Badge
                              bg={
                                STATUS_BADGES[product.moderationStatus]?.bg ||
                                "secondary"
                              }
                              className="opacity-75"
                            >
                              {STATUS_BADGES[product.moderationStatus]?.label ||
                                product.moderationStatus}
                            </Badge>
                          </div>
                        )}

                        <div className={styles.category}>
                          <CategoryBadge category={product.category} />
                        </div>

                        <div className={styles.macros}>
                          <span>К: {product.calories}</span>
                          <span>Б: {product.proteins}</span>
                          <span>Ж: {product.fats}</span>
                          <span>У: {product.carbohydrates}</span>
                        </div>

                        <div className={styles.bottomSection}>
                          <Badge bg="success" className={styles.ratingBadge}>
                            ★{" "}
                            {product.userRating
                              ? product.userRating.toFixed(1)
                              : "0.0"}
                          </Badge>
                          <span className={styles.reviewsText}>
                            {product.reviewsCount} отзывов
                          </span>
                        </div>
                      </div>
                    </div>
                  </Col>
                );
              })}
            </Row>
          )}
          {renderPagination()}
        </>
      )}
    </Container>
  );
};

export default ProductsPage;
