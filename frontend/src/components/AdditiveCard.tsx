import React from "react";
import styles from "./AdditiveCard.module.css";
import type { Additive, DangerLevel } from "../types/additives";
import { originTranslations } from "../pages/AdditivesPage";

const DANGER_LEVELS: DangerLevel[] = ["SAFE", "WARNING", "DANGEROUS", "BANNED"];

interface AdditiveCardProps {
  additive: Additive;
  onClick?: (id: string) => void;
}

const AdditiveCard: React.FC<AdditiveCardProps> = ({ additive, onClick }) => {
  return (
    <div
      className={styles.additiveCard}
      onClick={() => onClick && onClick(String(additive.id))}
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
            <span className={`${styles.originBadge} ${styles.bgOriginDEFAULT}`}>
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
  );
};

export default AdditiveCard;
