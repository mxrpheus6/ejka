import React, { useState, useEffect } from "react";
import { Row, Col, Form, Button, InputGroup } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import styles from "./ProductsFilter.module.css";

export const PRODUCT_CATEGORIES: Record<string, string> = {
  BEVERAGES: "Напитки",
  FATS_AND_OILS: "Жиры и масла",
  NUTS_AND_SEEDS: "Орехи и семечки",
  CEREALS_AND_LEGUMES: "Злаки и бобовые",
  MEAT_AND_FISH: "Мясо и рыба",
  SNACKS_AND_SWEETS: "Снеки и сладости",
  SAUCES: "Соусы",
  DAIRY: "Молочные продукты",
  FRUITS_AND_VEGETABLES: "Фрукты и овощи",
  GENERAL: "Прочее",
};

export const MODERATION_STATUSES: Record<string, string> = {
  PENDING: "На модерации",
  APPROVED: "Одобрено",
  REJECTED: "Отклонено",
};

interface ProductsFilterProps {
  showStatus?: boolean;
}

const ProductsFilter: React.FC<ProductsFilterProps> = ({
  showStatus = false,
}) => {
  const [searchParams, setSearchParams] = useSearchParams();

  // Получаем параметры из URL
  const searchQueryParam = searchParams.get("searchQuery") || "";
  const categoryParam = searchParams.get("category") || "";
  const statusParam = searchParams.get("status") || "";
  const minCaloriesParam = searchParams.get("minCalories") || "";
  const maxCaloriesParam = searchParams.get("maxCalories") || "";
  const minUserRatingParam = searchParams.get("minUserRating") || "";
  const additiveIdsParam = searchParams.getAll("additiveIds");

  // Получаем параметры сортировки (по умолчанию: новые)
  const sortByParam = searchParams.get("sortBy") || "createdAt";
  const sortDirectionParam = searchParams.get("sortDirection") || "desc";

  const hasFilters = Boolean(
    searchQueryParam ||
    categoryParam ||
    statusParam ||
    minCaloriesParam ||
    maxCaloriesParam ||
    minUserRatingParam ||
    additiveIdsParam.length > 0 ||
    sortDirectionParam === "asc", // Если выбрали "Сначала старые", кнопка сброса тоже активируется
  );

  const [localSearch, setLocalSearch] = useState(searchQueryParam);
  const [localMinCal, setLocalMinCal] = useState(minCaloriesParam);
  const [localMaxCal, setLocalMaxCal] = useState(maxCaloriesParam);
  const [localMinRating, setLocalMinRating] = useState(minUserRatingParam);

  useEffect(() => {
    setLocalSearch(searchQueryParam);
    setLocalMinCal(minCaloriesParam);
    setLocalMaxCal(maxCaloriesParam);
    setLocalMinRating(minUserRatingParam);
  }, [
    searchQueryParam,
    minCaloriesParam,
    maxCaloriesParam,
    minUserRatingParam,
  ]);

  const handleParamChange = (key: string, value: string) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set("page", "1");
    if (value) {
      newParams.set(key, value);
    } else {
      newParams.delete(key);
    }
    setSearchParams(newParams);
  };

  // Обработчик для комбинированной сортировки
  const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const [newSortBy, newSortDir] = e.target.value.split("-");
    const newParams = new URLSearchParams(searchParams);
    newParams.set("page", "1");
    newParams.set("sortBy", newSortBy);
    newParams.set("sortDirection", newSortDir);
    setSearchParams(newParams);
  };

  const resetFilters = () => {
    setSearchParams({ page: "1", sortBy: "createdAt", sortDirection: "desc" });
  };

  return (
    <div className={styles.filterWrapper}>
      <Row className="g-4 mb-4">
        <Col md={showStatus ? 5 : 8}>
          <Form.Group>
            <label className={styles.filterLabel}>Поиск</label>
            <Form.Control
              type="text"
              placeholder="Название или штрихкод..."
              className={styles.filterSelect}
              value={localSearch}
              onChange={(e) => setLocalSearch(e.target.value)}
              onBlur={() => handleParamChange("searchQuery", localSearch)}
              onKeyDown={(e) =>
                e.key === "Enter" &&
                handleParamChange("searchQuery", localSearch)
              }
            />
          </Form.Group>
        </Col>

        {showStatus && (
          <Col md={4}>
            <Form.Group>
              <label className={styles.filterLabel}>Статус модерации</label>
              <Form.Select
                className={styles.filterSelect}
                value={statusParam}
                onChange={(e) => handleParamChange("status", e.target.value)}
              >
                <option value="">Любой статус</option>
                {Object.entries(MODERATION_STATUSES).map(([key, label]) => (
                  <option key={key} value={key}>
                    {label}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
          </Col>
        )}

        <Col md={showStatus ? 3 : 4}>
          <Form.Group>
            <label className={styles.filterLabel}>Сортировка</label>
            <Form.Select
              className={styles.filterSelect}
              value={`${sortByParam}-${sortDirectionParam}`}
              onChange={handleSortChange}
            >
              <option value="createdAt-desc">Сначала новые</option>
              <option value="createdAt-asc">Сначала старые</option>
            </Form.Select>
          </Form.Group>
        </Col>
      </Row>

      <Row className="g-4 align-items-end">
        <Col lg={3} md={6}>
          <Form.Group>
            <label className={styles.filterLabel}>Категория</label>
            <Form.Select
              className={styles.filterSelect}
              value={categoryParam}
              onChange={(e) => handleParamChange("category", e.target.value)}
            >
              <option value="">Все категории</option>
              {Object.entries(PRODUCT_CATEGORIES).map(([key, label]) => (
                <option key={key} value={key}>
                  {label}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </Col>

        <Col lg={3} md={6}>
          <Form.Group>
            <label className={styles.filterLabel}>Калории (100г)</label>
            <InputGroup>
              <Form.Control
                type="number"
                placeholder="От"
                className={styles.filterSelect}
                value={localMinCal}
                onChange={(e) => setLocalMinCal(e.target.value)}
                onBlur={() => handleParamChange("minCalories", localMinCal)}
              />
              <InputGroup.Text className="bg-transparent border-0 px-1 text-muted">
                -
              </InputGroup.Text>
              <Form.Control
                type="number"
                placeholder="До"
                className={styles.filterSelect}
                value={localMaxCal}
                onChange={(e) => setLocalMaxCal(e.target.value)}
                onBlur={() => handleParamChange("maxCalories", localMaxCal)}
              />
            </InputGroup>
          </Form.Group>
        </Col>

        <Col lg={3} md={6}>
          <Form.Group>
            <label className={styles.filterLabel}>Мин. рейтинг ★</label>
            <Form.Control
              type="number"
              step="0.5"
              min="0"
              max="5"
              placeholder="0.0"
              className={styles.filterSelect}
              value={localMinRating}
              onChange={(e) => setLocalMinRating(e.target.value)}
              onBlur={() => handleParamChange("minUserRating", localMinRating)}
            />
          </Form.Group>
        </Col>

        <Col lg={3} md={6}>
          <Button
            variant="outline-secondary"
            className={`w-100 ${styles.filterResetBtn}`}
            onClick={resetFilters}
            disabled={!hasFilters}
          >
            Сбросить всё
          </Button>
        </Col>
      </Row>
    </div>
  );
};

export default ProductsFilter;
