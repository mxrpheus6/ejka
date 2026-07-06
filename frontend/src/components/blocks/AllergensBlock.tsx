import React from "react";
import styles from "./IngredientBlocks.module.css";

const ALLERGEN_EMOJIS: Record<string, string> = {
  PEANUT: "🥜",
  ASPARTAME_SULFITES: "🧪",
  MUSTARD: "🌿",
  GLUTEN: "🌾",
  SESAME: "🥯",
  LUPIN: "🌸",
  MOLLUSCS: "🦪",
  CRUSTACEANS: "🦐",
  MILK: "🥛",
  NUTS: "🌰",
  FISH: "🐟",
  CELERY: "🥬",
  SOY: "🌱",
  EGGS: "🥚",
};

const ALLERGEN_NAMES_RU: Record<string, string> = {
  PEANUT: "Арахис",
  ASPARTAME_SULFITES: "Аспартам и сульфиты",
  MUSTARD: "Горчица",
  GLUTEN: "Злаки (глютен)",
  SESAME: "Кунжут",
  LUPIN: "Люпин",
  MOLLUSCS: "Моллюски",
  CRUSTACEANS: "Ракообразные",
  MILK: "Молоко и лактоза",
  NUTS: "Орехи",
  FISH: "Рыба",
  CELERY: "Сельдерей",
  SOY: "Соя",
  EGGS: "Яйца",
};

export interface AllergenItem {
  category: string;
  matchedText?: string;
  score?: number;
}

interface AllergensBlockProps {
  allergens?: AllergenItem[] | null;
}

const AllergensBlock: React.FC<AllergensBlockProps> = ({ allergens }) => {
  return (
    <div className="mt-5 mb-5">
      <h3 className="fw-bold text-dark mb-4">Аллергены</h3>

      {allergens && allergens.length > 0 ? (
        <div className={styles.ingredientGrid}>
          {allergens.map((allergen, idx) => (
            <div
              key={idx}
              className={`${styles.ingredientCard} ${styles.allergenBorder}`}
            >
              <div className={styles.ingredientIconBox}>
                <span className={styles.ingredientEmoji}>
                  {ALLERGEN_EMOJIS[allergen.category] || "⚠️"}
                </span>
              </div>
              <div className={styles.ingredientInfo}>
                <div className={styles.ingredientCategory}>
                  {ALLERGEN_NAMES_RU[allergen.category] || allergen.category}
                </div>

                {allergen.matchedText && (
                  <div className={styles.ingredientMatch}>
                    Найдено:{" "}
                    <span className={styles.highlightText}>
                      {allergen.matchedText}
                    </span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className={`${styles.noAdditivesCard} ${styles.bgSafeBlue}`}>
          <div className={styles.noAdditivesIcon}>🛡️</div>
          <div>
            <h4 className={styles.noAdditivesTitle}>Без основных аллергенов</h4>
            <p className={styles.noAdditivesText}>
              Мы не нашли в составе следов наиболее распространенных аллергенов.
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default AllergensBlock;
