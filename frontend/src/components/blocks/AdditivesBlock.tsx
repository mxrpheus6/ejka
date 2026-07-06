import React from "react";
import { Row, Col } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import AdditiveCard from "../AdditiveCard";
import styles from "./IngredientBlocks.module.css";

interface AdditivesBlockProps {
  additives?: any[] | null;
  title?: string;
  emptyTitle?: string;
  emptyText?: string;
  headerAction?: React.ReactNode;
}

const AdditivesBlock: React.FC<AdditivesBlockProps> = ({
  additives,
  title = "Состав добавок",
  emptyTitle = "Идеально чистый состав!",
  emptyText = "В этом продукте не содержится никаких дополнительных пищевых добавок (Е-кодов).",
  headerAction,
}) => {
  const navigate = useNavigate();
  const count = additives?.length || 0;

  return (
    <div className="mt-5 mb-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3 className="fw-bold text-dark mb-0">
          {title} ({count})
        </h3>
        {headerAction && <div>{headerAction}</div>}
      </div>

      {count === 0 ? (
        <div className={`${styles.noAdditivesCard} ${styles.bgSafeGreen}`}>
          <div className={styles.noAdditivesIcon}>🌿</div>
          <div>
            <h4 className={styles.noAdditivesTitle}>{emptyTitle}</h4>
            <p className={styles.noAdditivesText}>{emptyText}</p>
          </div>
        </div>
      ) : (
        <Row className="g-4 align-items-stretch">
          {additives!.map((additive) => (
            <Col key={additive.id} xs={12} sm={6} md={4} lg={3}>
              <AdditiveCard
                additive={additive}
                onClick={(id) => navigate(`/additives/${id}`)}
              />
            </Col>
          ))}
        </Row>
      )}
    </div>
  );
};

export default AdditivesBlock;
