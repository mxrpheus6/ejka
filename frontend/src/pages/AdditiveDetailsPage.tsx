import React, { useEffect, useState } from "react";
import { Container, Spinner, Button, Alert } from "react-bootstrap";
import { useParams, useNavigate } from "react-router-dom";
import { additivesApi } from "../api/additivesApi";
import type { Additive, DangerLevel } from "../types/additives";
import { originTranslations } from "./AdditivesPage";
import styles from "./AdditiveDetailsPage.module.css";

const DANGER_LEVELS: DangerLevel[] = ["SAFE", "WARNING", "DANGEROUS", "BANNED"];

const DANGER_LEVEL_LABELS: Record<DangerLevel, string> = {
  SAFE: "Безопасная",
  WARNING: "Требует внимания",
  DANGEROUS: "Не рекомендуется",
  BANNED: "Запрещена в РБ",
};

const AdditiveDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [additive, setAdditive] = useState<Additive | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchAdditiveDetails = async () => {
      try {
        const data = await additivesApi.getAdditiveById(id as string);
        setAdditive(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchAdditiveDetails();
    }
  }, [id]);

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

  if (!additive) {
    return (
      <Container className="mt-5 text-center">
        <Alert variant="danger" className="shadow-sm border-0 rounded-4">
          Добавка не найдена
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

  return (
    <Container className="mt-4 pb-5">
      <Button
        variant="light"
        className="mb-4 rounded-pill px-4 shadow-sm fw-bold text-secondary"
        onClick={() => navigate(-1)}
      >
        &larr; Назад к списку
      </Button>

      <div className={styles.detailsCard}>
        <div className={styles.headerRow}>
          <div>
            <div className={styles.code}>{additive.code}</div>
            <div className={styles.category}>{additive.category}</div>
          </div>
          <div className={styles.dangerIndicatorWrapper}>
            {DANGER_LEVELS.map((level) => (
              <div key={level} className={styles.dangerItem}>
                <div
                  className={`${styles.rect} ${styles[`bg${level}`]} ${additive.dangerLevel === level ? styles.rectActive : ""}`}
                />
                <span
                  className={`${styles.dangerLabel} ${additive.dangerLevel === level ? styles.labelActive : ""}`}
                >
                  {DANGER_LEVEL_LABELS[level]}
                </span>
              </div>
            ))}
          </div>
        </div>

        <h2 className={styles.nameRu}>{additive.nameRu}</h2>
        <p className={styles.nameEn}>{additive.nameEn}</p>

        {additive.warningDescription && (
          <Alert
            variant="warning"
            className="mt-4 border-0 shadow-sm rounded-4"
          >
            <strong>Внимание!</strong> {additive.warningDescription}
          </Alert>
        )}

        <div className={styles.sectionTitle}>Описание</div>
        <p className={styles.description}>
          {additive.description || "Описание отсутствует."}
        </p>

        <div className={styles.originsSection}>
          <div className={styles.originsTitle}>Происхождение</div>
          <div className={styles.originsList}>
            {additive.origins && additive.origins.length > 0 ? (
              additive.origins.map((origin) => (
                <span
                  key={origin.id}
                  className={`${styles.originBadge} ${styles[`bgOrigin${origin.type}`] || styles.bgOriginDEFAULT}`}
                >
                  {originTranslations[origin.type]
                    ? `${originTranslations[origin.type]} происхождение`
                    : origin.type}
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
        </div>
      </div>
    </Container>
  );
};

export default AdditiveDetailsPage;
