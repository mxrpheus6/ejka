import React, { useEffect, useState, useCallback } from "react";
import {
  Container,
  Spinner,
  Button,
  Alert,
  Row,
  Col,
  Carousel,
  OverlayTrigger,
  Tooltip,
  Modal,
  Badge,
} from "react-bootstrap";
import { useParams, useNavigate } from "react-router-dom";
import { productsApi } from "../api/productsApi";
import type { Product, ImpactLevel } from "../types/products";
import styles from "./ProductsDetailsPage.module.css";
import AuthModal from "../components/AuthModal";
import ProductReviews from "./ProductReviews";
import CategoryBadge from "../components/CategoryBadge";

// Импортируем наши новые вынесенные блоки
import AdditivesBlock from "../components/blocks/AdditivesBlock";
import AllergensBlock from "../components/blocks/AllergensBlock";
import ControversialBlock from "../components/blocks/ControversialBlock";

const MINIO_BASE_URL = import.meta.env.VITE_MINIO_PRODUCTS_URL || "";

const COLORS = {
  EXCELLENT: "#10b981",
  GOOD: "#34d399",
  POOR: "#fbbf24",
  BAD: "#ef4444",
};

const MACRO_TRANSLATIONS: Record<string, string> = {
  kcal: "Калорийность",
  proteins: "Белки",
  fats: "Жиры",
  carbs: "Углеводы",
  carbohydrates: "Углеводы",
  sugars: "Сахар",
  salt: "Соль",
  sodium: "Натрий",
  fiber: "Клетчатка",
};

const STATUS_BADGES: Record<string, { label: string; bg: string }> = {
  PENDING: { label: "На проверке", bg: "warning" },
  APPROVED: { label: "Проверен", bg: "success" },
  REJECTED: { label: "Отклонен", bg: "danger" },
};

const getScoreColor = (score?: number | null) => {
  if (score === undefined || score === null) return "#cbd5e1";
  if (score >= 75) return COLORS.EXCELLENT;
  if (score >= 50) return COLORS.GOOD;
  if (score >= 25) return COLORS.POOR;
  return COLORS.BAD;
};

const getImpactColor = (impact: ImpactLevel) => {
  switch (impact) {
    case "EXCELLENT":
      return COLORS.EXCELLENT;
    case "GOOD":
      return COLORS.GOOD;
    case "POOR":
      return COLORS.POOR;
    case "BAD":
      return COLORS.BAD;
    default:
      return "#cbd5e1";
  }
};

const getImpactText = (impact: ImpactLevel, higherBetter?: boolean) => {
  if (higherBetter) {
    return impact === "EXCELLENT" ? "Отлично" : "Норма";
  }
  if (impact === "EXCELLENT") return "Отлично";
  if (impact === "GOOD") return "Норма";
  if (impact === "POOR") return "Много";
  if (impact === "BAD") return "Слишком много";
  return impact;
};

const calculateMarkerPosition = (
  val: number,
  thresholds: number[],
  higherBetter: boolean,
): number => {
  if (higherBetter) {
    const targetThreshold = Math.max(...thresholds);
    if (val <= targetThreshold) {
      if (targetThreshold === 0) return 0;
      return (val / targetThreshold) * 50;
    } else {
      const overflow = targetThreshold || 8;
      return Math.min(98, 50 + ((val - targetThreshold) / overflow) * 50);
    }
  } else {
    const sorted = [...thresholds].sort((a, b) => a - b);
    const [t1, t2, t3] = sorted;

    if (val <= t1) return t1 === 0 ? 0 : (val / t1) * 25;
    if (val <= t2) return 25 + ((val - t1) / (t2 - t1)) * 25;
    if (val <= t3) return 50 + ((val - t2) / (t3 - t2)) * 25;

    const overflow = t3 * 0.5 || 10;
    return Math.min(98, 75 + ((val - t3) / overflow) * 25);
  }
};

const AnimatedMarker: React.FC<{ targetPosition: number; color: string }> = ({
  targetPosition,
  color,
}) => {
  const [pos, setPos] = useState(0);

  useEffect(() => {
    const timer = setTimeout(() => {
      setPos(targetPosition);
    }, 50);
    return () => clearTimeout(timer);
  }, [targetPosition]);

  return (
    <div
      className={styles.marker}
      style={{
        left: `${pos}%`,
        borderTopColor: color,
      }}
    />
  );
};

const ProductDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [showAuthModal, setShowAuthModal] = useState<boolean>(false);
  const [expandedMacros, setExpandedMacros] = useState<Record<string, boolean>>(
    {},
  );

  const [showImageModal, setShowImageModal] = useState<boolean>(false);
  const [currentImageIdx, setCurrentImageIdx] = useState<number>(0);

  const fetchDetails = useCallback(async () => {
    if (!id) return;
    try {
      const data = await productsApi.getProductById(id);
      setProduct(data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchDetails();
  }, [fetchDetails]);

  // Блокировка скролла при открытой модалке
  useEffect(() => {
    if (showImageModal) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }
    return () => {
      document.body.style.overflow = "";
    };
  }, [showImageModal]);

  const toggleMacro = (macroName: string) => {
    setExpandedMacros((prev) => ({
      ...prev,
      [macroName]: !prev[macroName],
    }));
  };

  const handleImageClick = (idx: number) => {
    setCurrentImageIdx(idx);
    setShowImageModal(true);
  };

  if (loading) {
    return (
      <Container
        className="mt-5 d-flex justify-content-center align-items-center"
        style={{ minHeight: "50vh" }}
      >
        <Spinner
          animation="border"
          variant="success"
          style={{ width: "3rem", height: "3rem" }}
        />
      </Container>
    );
  }

  if (!product) {
    return (
      <Container className="mt-5 text-center">
        <Alert variant="danger" className="shadow-sm border-0 rounded-4">
          Продукт не найден
        </Alert>
        <Button
          variant="outline-dark"
          className="rounded-pill px-4"
          onClick={() => navigate(-1)}
        >
          Назад
        </Button>
      </Container>
    );
  }

  const sortedImages = [...(product.images || [])].sort((a, b) => {
    if (a.type === "MAIN") return -1;
    if (b.type === "MAIN") return 1;
    return 0;
  });

  return (
    <Container className="mt-4 pb-5">
      <Button
        variant="light"
        className="mb-4 rounded-pill px-4 shadow-sm fw-bold text-secondary"
        onClick={() => navigate(-1)}
      >
        &larr; Назад к каталогу
      </Button>

      <div className={styles.detailsCard}>
        <Row className="align-items-start gy-4">
          <Col lg={5} md={12}>
            <div className={styles.imageContainer}>
              {(() => {
                if (sortedImages.length === 0)
                  return <div className={styles.noImage}>Нет фото</div>;

                if (sortedImages.length === 1) {
                  return (
                    <img
                      src={`${MINIO_BASE_URL}${sortedImages[0].objectKey}`}
                      alt={product.title}
                      className={styles.mainImage}
                      onClick={() => handleImageClick(0)}
                    />
                  );
                }

                return (
                  <Carousel
                    activeIndex={currentImageIdx}
                    onSelect={(selectedIndex) =>
                      setCurrentImageIdx(selectedIndex)
                    }
                    className={styles.carousel}
                    interval={null}
                    variant="dark"
                    indicators={false}
                  >
                    {sortedImages.map((img, idx) => (
                      <Carousel.Item key={idx} className={styles.carouselItem}>
                        <div className={styles.slideContent}>
                          <img
                            src={`${MINIO_BASE_URL}${img.objectKey}`}
                            alt={`${product.title} - ${idx + 1}`}
                            className={styles.mainImage}
                            onClick={() => handleImageClick(idx)}
                          />
                        </div>
                      </Carousel.Item>
                    ))}
                  </Carousel>
                );
              })()}
            </div>
          </Col>

          <Col lg={7} md={12}>
            <div className={styles.header}>
              <CategoryBadge
                category={product.category}
                className={styles.category}
              />{" "}
              <div className={styles.barcode}>Штрихкод: {product.barcode}</div>
            </div>

            <div className={styles.scoreHeader}>
              <div className={styles.titleContainer}>
                <div className="d-flex align-items-center gap-3 mb-2 flex-wrap">
                  <h2 className={`${styles.title} mb-0`}>{product.title}</h2>

                  {product.moderationStatus && (
                    <Badge
                      bg={
                        STATUS_BADGES[product.moderationStatus]?.bg ||
                        "secondary"
                      }
                      className="px-2 py-1 rounded-pill"
                      style={{
                        fontSize: "0.85rem",
                        fontWeight: "600",
                        opacity: 0.85,
                      }}
                    >
                      {STATUS_BADGES[product.moderationStatus]?.label ||
                        product.moderationStatus}
                    </Badge>
                  )}
                </div>

                {product.authorUsername && (
                  <div
                    className="text-muted mb-2"
                    style={{ fontSize: "0.85rem" }}
                  >
                    👤 Добавил(а):{" "}
                    <span className="fw-semibold text-dark">
                      {product.authorUsername}
                    </span>
                  </div>
                )}

                <div
                  className={styles.ratingRow}
                  style={{ display: "flex", alignItems: "center", gap: "8px" }}
                >
                  <span className={styles.userRating}>
                    ★{" "}
                    {product.userRating ? product.userRating.toFixed(1) : "0.0"}
                  </span>
                  <span
                    className="text-muted fw-normal"
                    style={{ fontSize: "0.85rem" }}
                  >
                    Пользовательский рейтинг
                  </span>
                </div>
              </div>

              {product.nutritionScore !== undefined &&
                product.nutritionScore !== null && (
                  <OverlayTrigger
                    placement="bottom"
                    overlay={
                      <Tooltip id={`tooltip-score-${product.id}`}>
                        Внутренний рейтинг качества (0-100)
                      </Tooltip>
                    }
                  >
                    <div
                      className={styles.bigScoreCircle}
                      style={{
                        backgroundColor: getScoreColor(product.nutritionScore),
                      }}
                    >
                      <span className={styles.bigScoreValue}>
                        {product.nutritionScore}
                      </span>
                      <span className={styles.bigScoreMax}>/100</span>
                    </div>
                  </OverlayTrigger>
                )}
            </div>

            {product.scoreDetails?.macros &&
            product.scoreDetails.macros.length > 0 ? (
              <div
                style={{
                  display: "flex",
                  flexDirection: "column",
                  gap: "12px",
                }}
              >
                {product.scoreDetails.macros.map((macro, idx) => {
                  const color = getImpactColor(macro.impact);

                  const displayName =
                    MACRO_TRANSLATIONS[macro.name.toLowerCase()] || macro.name;

                  const isExpanded = !!expandedMacros[macro.name];
                  const hasSliderData =
                    macro.numericValue !== undefined &&
                    macro.thresholds &&
                    macro.thresholds.length > 0;

                  const sortedThresholds = hasSliderData
                    ? [...macro.thresholds!].sort((a, b) => a - b)
                    : [];
                  const targetThreshold = hasSliderData
                    ? Math.max(...macro.thresholds!)
                    : 0;

                  const segmentColors = macro.higherBetter
                    ? [COLORS.GOOD, COLORS.EXCELLENT]
                    : [COLORS.EXCELLENT, COLORS.GOOD, COLORS.POOR, COLORS.BAD];

                  return (
                    <div key={idx} className={styles.macroRowWrapper}>
                      <div
                        className={styles.macroRowClickable}
                        onClick={() => hasSliderData && toggleMacro(macro.name)}
                        style={{
                          cursor: hasSliderData ? "pointer" : "default",
                        }}
                      >
                        <div className={styles.macroInfo}>
                          <span className={styles.macroName}>
                            {displayName}
                          </span>
                          <span className={styles.macroValue}>
                            {macro.value}
                          </span>
                        </div>

                        <div className={styles.macroImpactWrapper}>
                          <span
                            className={styles.macroImpactText}
                            style={{ color: color }}
                          >
                            {getImpactText(macro.impact, macro.higherBetter)}
                          </span>
                          <span
                            className={styles.impactDot}
                            style={{ backgroundColor: color }}
                          />
                          {hasSliderData && (
                            <span
                              className={`${styles.expandIcon} ${isExpanded ? styles.rotated : ""}`}
                            >
                              ▼
                            </span>
                          )}
                        </div>
                      </div>

                      {isExpanded && hasSliderData && (
                        <div className={styles.sliderContainer}>
                          <div className={styles.sliderTrack}>
                            {segmentColors.map((segColor, i) => (
                              <div
                                key={i}
                                className={styles.sliderSegment}
                                style={{ backgroundColor: segColor }}
                              />
                            ))}

                            <AnimatedMarker
                              targetPosition={calculateMarkerPosition(
                                macro.numericValue!,
                                macro.thresholds!,
                                macro.higherBetter!,
                              )}
                              color={color}
                            />
                          </div>

                          <div className={styles.thresholdLabels}>
                            {macro.higherBetter ? (
                              <span
                                className={styles.labelTick}
                                style={{ left: "50%" }}
                              >
                                {targetThreshold}
                              </span>
                            ) : (
                              <>
                                <span
                                  className={styles.labelTick}
                                  style={{ left: "25%" }}
                                >
                                  {sortedThresholds[0]}
                                </span>
                                <span
                                  className={styles.labelTick}
                                  style={{ left: "50%" }}
                                >
                                  {sortedThresholds[1]}
                                </span>
                                <span
                                  className={styles.labelTick}
                                  style={{ left: "75%" }}
                                >
                                  {sortedThresholds[2]}
                                </span>
                              </>
                            )}
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className={styles.macrosBox}>
                <div className={styles.macroItem}>
                  <span className={styles.macroValue}>{product.calories}</span>
                  <span className={styles.macroLabel}>Ккал</span>
                </div>
                <div className={styles.macroItem}>
                  <span className={styles.macroValue}>
                    {product.proteins} г
                  </span>
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
            )}
          </Col>
        </Row>
      </div>

      {product.compositionText && (
        <div
          className="mt-4 p-4 rounded-4 shadow-sm bg-white"
          style={{ border: "1px solid #f8f9fa" }}
        >
          <h5 className="fw-bold mb-3 text-dark">Состав</h5>
          <p
            className="text-secondary mb-0"
            style={{
              lineHeight: "1.6",
              whiteSpace: "pre-wrap",
              fontSize: "0.95rem",
            }}
          >
            {product.compositionText}
          </p>
        </div>
      )}

      <AdditivesBlock
        additives={product.additives}
        title="Список добавок"
        emptyText="В этом продукте не содержится никаких дополнительных пищевых добавок (Е-кодов). Отличный выбор для здорового питания!"
      />
      <AllergensBlock
        allergens={product.allergens?.map((allergenEnum) => ({
          category: allergenEnum,
        }))}
      />
      <ControversialBlock
        controversial={
          product.hasPalmOil ? [{ category: "Пальмовое масло" }] : []
        }
      />

      <ProductReviews
        productId={id as string}
        reviewsCount={product.reviewsCount}
        setShowAuthModal={setShowAuthModal}
        onReviewChanged={fetchDetails}
      />

      <AuthModal show={showAuthModal} onHide={() => setShowAuthModal(false)} />

      <Modal
        show={showImageModal}
        onHide={() => setShowImageModal(false)}
        fullscreen={true}
        contentClassName={styles.imageModalContent}
      >
        <Modal.Header closeButton className={styles.imageModalHeader} />
        <Modal.Body className="p-0 d-flex justify-content-center align-items-center">
          {(() => {
            if (sortedImages.length <= 1) {
              return (
                <div className={styles.modalSlideContent}>
                  <img
                    src={`${MINIO_BASE_URL}${sortedImages[0]?.objectKey}`}
                    alt={product?.title}
                    className={styles.modalImage}
                  />
                </div>
              );
            }

            return (
              <Carousel
                activeIndex={currentImageIdx}
                onSelect={(selectedIndex) => setCurrentImageIdx(selectedIndex)}
                interval={null}
                variant="dark"
                className="w-100"
              >
                {sortedImages.map((img, idx) => (
                  <Carousel.Item key={idx}>
                    <div className={styles.modalSlideContent}>
                      <img
                        src={`${MINIO_BASE_URL}${img.objectKey}`}
                        alt={`${product?.title} - ${idx + 1}`}
                        className={styles.modalImage}
                      />
                    </div>
                  </Carousel.Item>
                ))}
              </Carousel>
            );
          })()}
        </Modal.Body>
      </Modal>
    </Container>
  );
};

export default ProductDetailsPage;
