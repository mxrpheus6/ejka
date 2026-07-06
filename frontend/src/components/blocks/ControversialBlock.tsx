import React from "react";
import styles from "./IngredientBlocks.module.css";

const CONTROVERSIAL_EMOJIS: Record<string, string> = {
  "Пальмовое масло": "🌴",
  "Скрытые/растительные жиры": "🕵️‍♂️",
};

// Гибкий интерфейс для переиспользования в сканере и каталоге
export interface ControversialItem {
  category: string;
  matchedText?: string;
  score?: number;
}

interface ControversialBlockProps {
  controversial?: ControversialItem[] | null;
}

const ControversialBlock: React.FC<ControversialBlockProps> = ({
  controversial,
}) => {
  return (
    <div className="mt-5 mb-5">
      <h3 className="fw-bold text-dark mb-4">Спорные ингредиенты</h3>
      {controversial && controversial.length > 0 ? (
        <div className={styles.ingredientGrid}>
          {controversial.map((item, idx) => (
            <div
              key={idx}
              className={`${styles.ingredientCard} ${styles.controversialBorder}`}
            >
              <div className={styles.ingredientIconBox}>
                <span className={styles.ingredientEmoji}>
                  {CONTROVERSIAL_EMOJIS[item.category] || "🧐"}
                </span>
              </div>
              <div className={styles.ingredientInfo}>
                <div className={styles.ingredientCategory}>{item.category}</div>

                {/* Если matchedText есть - выводим его, если нет - блок просто не отрендерится */}
                {item.matchedText && (
                  <div className={styles.ingredientMatch}>
                    Найдено:{" "}
                    <span className={styles.highlightText}>
                      {item.matchedText}
                    </span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className={`${styles.noAdditivesCard} ${styles.bgSafeGreen}`}>
          <div className={styles.noAdditivesIcon}>✨</div>
          <div>
            <h4 className={styles.noAdditivesTitle}>Нет спорных компонентов</h4>
            <p className={styles.noAdditivesText}>
              На этикетке не обнаружено пальмового масла или скрытых
              растительных жиров.
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default ControversialBlock;
