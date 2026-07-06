import React from "react";
import { Badge } from "react-bootstrap";
import type { ProductCategory } from "../types/products";
import { ProductCategoryLabels } from "../types/products";

interface CategoryBadgeProps {
  category: ProductCategory | string;
  className?: string;
}

const CategoryBadge: React.FC<CategoryBadgeProps> = ({
  category,
  className = "",
}) => {
  const label = ProductCategoryLabels[category as ProductCategory] || category;

  return (
    <span
      className={`${className} px-2 py-1 fw-semibold d-inline-block`}
      style={{
        backgroundColor: "#ff8c00",
        color: "#fff",
        fontSize: "0.8rem",
        borderRadius: "6px",
        lineHeight: "1",
      }}
    >
      {label.toUpperCase()}
    </span>
  );
};

export default CategoryBadge;
