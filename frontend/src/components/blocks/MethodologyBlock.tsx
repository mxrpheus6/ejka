import React from "react";
import styles from "./MethodologyBlock.module.css";

const MethodologyBlock: React.FC = () => {
  return (
    <section className={styles.methodologySection}>
      <div className={styles.container}>
        <div className={styles.header}>
          <h2 className={styles.title}>Научная и юридическая база</h2>
          Алгоритм анализа составов опирается на фундаментальную базу,
          объединяющую межгосударственные стандарты ЕАЭС, международные
          регламенты ФАО/ВОЗ, а также медицинские протоколы и национальные
          санитарные нормы <b>Республики Беларусь</b>.
        </div>

        <div className={styles.docsGrid}>
          {/* Документ 1: ТР ТС Аллергены */}
          <a
            href="https://eec.eaeunion.org/upload/medialibrary/9db/TrTsPishevkaMarkirovka.pdf"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.docCard}
          >
            <div className={styles.docCardHeader}>
              <div
                className={styles.docIconWrapper}
                style={{ backgroundColor: "#e0e7ff", color: "#4f46e5" }}
              >
                ⚖️
              </div>
              <span className={styles.docType}>Регламент ЕАЭС</span>
            </div>
            <h5 className={styles.docTitle}>ТР ТС 022/2011</h5>
            <p className={styles.docDescription}>
              «Пищевая продукция в части ее маркировки». Юридическая база,
              строго обязывающая производителей декларировать наиболее
              распространенные аллергены в составе продуктов.
            </p>
            <div className={styles.docLinkArrow}>Читать документ →</div>
          </a>

          {/* Документ 2: ТР ТС Добавки (Е-шки) */}
          <a
            href="https://eec.eaeunion.org/comission/department/deptexreg/tr/bezopPischDobavok.php"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.docCard}
          >
            <div className={styles.docCardHeader}>
              <div
                className={styles.docIconWrapper}
                style={{ backgroundColor: "#fef08a", color: "#ca8a04" }}
              >
                🧪
              </div>
              <span className={styles.docType}>Регламент ЕАЭС</span>
            </div>
            <h5 className={styles.docTitle}>ТР ТС 029/2012</h5>
            <p className={styles.docDescription}>
              Требования безопасности пищевых добавок, ароматизаторов и
              технологических вспомогательных средств. Регламентирует
              разрешенные пищевые добавки (Е-коды).
            </p>
            <div className={styles.docLinkArrow}>Читать документ →</div>
          </a>

          {/* Документ 3: СанПиН РБ (Новый блок) */}
          <a
            href="https://pravo.by/document/?guid=12551&p0=W21326755p"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.docCard}
          >
            <div className={styles.docCardHeader}>
              <div
                className={styles.docIconWrapper}
                style={{ backgroundColor: "#ffedd5", color: "#ea580c" }}
              >
                📜
              </div>
              <span className={styles.docType}>Санитарные нормы и правила</span>
            </div>
            <h5 className={styles.docTitle}>Пост. Минздрава РБ № 195</h5>
            <p className={styles.docDescription}>
              Конкретизирует гигиенические требования к применению добавок и
              фиксирует национальные перечни веществ, оборот которых имеет
              ограничения на территории Республики Беларусь.
            </p>
            <div className={styles.docLinkArrow}>Читать документ →</div>
          </a>

          {/* Документ 4: Инструкция Минздрава */}
          <a
            href="http://med.by/methods/pdf/full/048-0622.pdf"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.docCard}
          >
            <div className={styles.docCardHeader}>
              <div
                className={styles.docIconWrapper}
                style={{ backgroundColor: "#dcfce7", color: "#16a34a" }}
              >
                🔬
              </div>
              <span className={styles.docType}>Инструкция Минздрава РБ</span>
            </div>
            <h5 className={styles.docTitle}>Метод. № 048-0622</h5>
            <p className={styles.docDescription}>
              Научно обоснованный метод эпидемиологического надзора.
              Обеспечивает корректное выявление скрытых аллергенов системой
              автоматического анализа.
            </p>
            <div className={styles.docLinkArrow}>Читать документ →</div>
          </a>

          {/* Документ 5: Codex Alimentarius / ВОЗ */}
          <a
            href="https://www.fao.org/fao-who-codexalimentarius/en/"
            target="_blank"
            rel="noopener noreferrer"
            className={styles.docCard}
          >
            <div className={styles.docCardHeader}>
              <div
                className={styles.docIconWrapper}
                style={{ backgroundColor: "#e0f2fe", color: "#0284c7" }}
              >
                🌍
              </div>
              <span className={styles.docType}>Стандарты ФАО/ВОЗ</span>
            </div>
            <h5 className={styles.docTitle}>Codex Alimentarius</h5>
            <p className={styles.docDescription}>
              Международные пищевые стандарты по оценке безопасности пищевых
              добавок (GSFA). Гарантируют соответствие системы глобальным
              критериям оценки.
            </p>
            <div className={styles.docLinkArrow}>Читать документ →</div>
          </a>
        </div>
      </div>
    </section>
  );
};

export default MethodologyBlock;
