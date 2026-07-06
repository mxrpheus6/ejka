import React, { useState, useEffect } from "react";
import { Row, Col, Form, Button } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import {
  ORIGIN_TRANSLATIONS,
  DANGER_LEVEL_TRANSLATIONS,
  CATEGORIES_LIST,
} from "../constants/additives";

import styles from "./AdditivesFilter.module.css";

const AdditivesFilter: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const [localSearch, setLocalSearch] = useState("");
  const [localCategory, setLocalCategory] = useState("");
  const [localDangerLevel, setLocalDangerLevel] = useState("");
  const [localOrigins, setLocalOrigins] = useState<string[]>([]);

  useEffect(() => {
    setLocalSearch(searchParams.get("searchQuery") || "");
    setLocalCategory(searchParams.get("category") || "");
    setLocalDangerLevel(searchParams.get("dangerLevel") || "");
    setLocalOrigins(searchParams.getAll("origin"));
  }, [searchParams]);

  const hasLocalFilters = Boolean(
    localSearch || localCategory || localDangerLevel || localOrigins.length > 0,
  );

  const handleOriginToggle = (originKey: string) => {
    if (localOrigins.includes(originKey)) {
      setLocalOrigins((prev) => prev.filter((o) => o !== originKey));
    } else {
      setLocalOrigins((prev) => [...prev, originKey]);
    }
  };

  const applyFilters = () => {
    const newParams = new URLSearchParams();
    if (localSearch.trim()) newParams.set("searchQuery", localSearch.trim());
    if (localCategory) newParams.set("category", localCategory);
    if (localDangerLevel) newParams.set("dangerLevel", localDangerLevel);
    localOrigins.forEach((o) => newParams.append("origin", o));
    newParams.set("page", "1");
    setSearchParams(newParams);
  };

  const resetFilters = () => {
    setLocalSearch("");
    setLocalCategory("");
    setLocalDangerLevel("");
    setLocalOrigins([]);
    setSearchParams({ page: "1" });
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      e.preventDefault();
      applyFilters();
    }
  };

  return (
    <div className={styles.filterWrapper}>
      {/* 1. РЯД ИНПУТОВ */}
      <Row className="g-4 mb-4">
        {/* Поиск */}
        <Col xs={12} lg={4}>
          <Form.Group>
            <label className={styles.filterLabel}>Поиск</label>
            <Form.Control
              className={styles.filterSelect}
              placeholder="Название или код (E300)..."
              value={localSearch}
              onChange={(e) => setLocalSearch(e.target.value)}
              onKeyDown={handleKeyDown}
            />
          </Form.Group>
        </Col>

        {/* Категория */}
        <Col xs={12} md={6} lg={4}>
          <Form.Group>
            <label className={styles.filterLabel}>Категория</label>
            <Form.Select
              className={styles.filterSelect}
              value={localCategory}
              onChange={(e) => setLocalCategory(e.target.value)}
            >
              <option value="">Все категории</option>
              {CATEGORIES_LIST.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </Col>

        {/* Опасность */}
        <Col xs={12} md={6} lg={4}>
          <Form.Group>
            <label className={styles.filterLabel}>Опасность</label>
            <Form.Select
              className={styles.filterSelect}
              value={localDangerLevel}
              onChange={(e) => setLocalDangerLevel(e.target.value)}
            >
              <option value="">Любая опасность</option>
              {Object.entries(DANGER_LEVEL_TRANSLATIONS).map(([key, label]) => (
                <option key={key} value={key}>
                  {label}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </Col>
      </Row>

      {/* 2. ТВОИ PILLS ПРОИСХОЖДЕНИЯ */}
      <Row className="mb-4">
        <Col>
          <label className={styles.filterLabel}>
            Происхождение (можно выбрать несколько)
          </label>
          <div className={styles.originPills}>
            {Object.entries(ORIGIN_TRANSLATIONS).map(([key, label]) => {
              const isActive = localOrigins.includes(key);
              return (
                <div
                  key={key}
                  className={`${styles.originPill} ${isActive ? styles.active : ""}`}
                  onClick={() => handleOriginToggle(key)}
                  role="button"
                  tabIndex={0}
                >
                  {label}
                </div>
              );
            })}
          </div>
        </Col>
      </Row>

      {/* 3. КНОПКИ УПРАВЛЕНИЯ (в твоем стиле) */}
      <div className="d-flex justify-content-end gap-3 pt-3">
        <Button
          variant="outline-secondary"
          className={styles.filterResetBtn}
          onClick={resetFilters}
          disabled={!hasLocalFilters}
        >
          Сбросить
        </Button>
        <Button
          variant="success"
          className="rounded-pill px-4 fw-bold"
          onClick={applyFilters}
          style={{ backgroundColor: "#539155", borderColor: "#539155" }}
        >
          Применить фильтры
        </Button>
      </div>
    </div>
  );
};

export default AdditivesFilter;
