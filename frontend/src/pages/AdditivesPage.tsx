import React, { useEffect, useState } from "react";
import {
  Container,
  Row,
  Col,
  Spinner,
  Pagination,
  Button,
} from "react-bootstrap";
import { useSearchParams, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { RootState } from "../store/store";
import { saveAdditivesPage } from "../store/additivesSlice";
import { additivesApi } from "../api/additivesApi";
import type { AdditivesResponse } from "../types/additives";
import AdditiveCard from "../components/AdditiveCard";
import AdditivesFilter from "../components/AdditivesFilter";
import { useAuthRole } from "../hooks/useAuthRole";

export { ORIGIN_TRANSLATIONS as originTranslations } from "../constants/additives";

const AdditivesPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  // === ПРОВЕРКА РОЛИ ДЛЯ РЕДАКТИРОВАНИЯ ===
  const { isModerator, role } = useAuthRole();
  const canEdit = isModerator || role === "ROLE_ADMIN";

  const currentPageStr = searchParams.get("page");
  const currentPage = currentPageStr ? parseInt(currentPageStr, 10) : 1;
  const offset = Math.max(0, currentPage - 1);

  const categoryParam = searchParams.get("category") || "";
  const dangerLevelParam = searchParams.get("dangerLevel") || "";
  const originsParam = searchParams.getAll("origin");

  // 👇 ДОСТАЕМ ПОИСКОВЫЙ ЗАПРОС ИЗ URL
  const searchQueryParam = searchParams.get("searchQuery") || "";

  // 👇 ДОБАВЛЯЕМ ЕГО В УСЛОВИЕ ПРОВЕРКИ ФИЛЬТРОВ
  const hasFilters = Boolean(
    categoryParam ||
    dangerLevelParam ||
    originsParam.length > 0 ||
    searchQueryParam,
  );

  const cachedData = useSelector((state: RootState) =>
    hasFilters ? null : state.additives.cache[currentPage],
  );

  const [data, setData] = useState<AdditivesResponse | null>(
    cachedData || null,
  );
  const [loading, setLoading] = useState<boolean>(!cachedData);

  const limit = 12;
  const sortBy = "code";
  const sortDirection = "asc";

  useEffect(() => {
    if (cachedData && !hasFilters) {
      setData(cachedData);
      setLoading(false);
      return;
    }

    const fetchAdditives = async () => {
      setLoading(true);

      try {
        const responseData = await additivesApi.getAdditives({
          offset,
          limit,
          sortBy,
          sortDirection,
          category: categoryParam || undefined,
          dangerLevel: dangerLevelParam || undefined,
          origin: originsParam.length > 0 ? originsParam : undefined,
          searchQuery: searchQueryParam || undefined,
        });

        setData(responseData);

        if (!hasFilters) {
          dispatch(
            saveAdditivesPage({ page: currentPage, data: responseData }),
          );
        }
      } catch (error) {
        console.error("Ошибка при загрузке добавок:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchAdditives();
  }, [
    currentPage,
    offset,
    limit,
    sortBy,
    sortDirection,
    cachedData,
    dispatch,
    hasFilters,
    categoryParam,
    dangerLevelParam,
    originsParam.join(","),
    searchQueryParam, // 👇 СЛЕДИМ ЗА ИЗМЕНЕНИЕМ ПОИСКА ДЛЯ ПОВТОРНОГО ЗАПРОСА
  ]);

  const handlePageChange = (pageNumber: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set("page", pageNumber.toString());
    setSearchParams(newParams);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleCardClick = (id: string | number) => {
    navigate(`/additives/${String(id)}`);
  };

  const renderPagination = () => {
    if (!data || data.totalPages <= 1) return null;

    const totalPages = data.totalPages;
    let items = [];

    items.push(
      <Pagination.First
        key="first"
        disabled={currentPage === 1}
        onClick={() => handlePageChange(1)}
      />,
    );

    items.push(
      <Pagination.Prev
        key="prev"
        disabled={currentPage === 1}
        onClick={() => handlePageChange(currentPage - 1)}
      />,
    );

    let startPage = Math.max(1, currentPage - 2);
    let endPage = Math.min(totalPages, currentPage + 2);

    if (currentPage <= 3) {
      endPage = Math.min(totalPages, 5);
    } else if (currentPage >= totalPages - 2) {
      startPage = Math.max(1, totalPages - 4);
    }

    if (startPage > 1) {
      items.push(
        <Pagination.Item key={1} onClick={() => handlePageChange(1)}>
          1
        </Pagination.Item>,
      );

      if (startPage > 2)
        items.push(<Pagination.Ellipsis key="ell-start" disabled />);
    }

    for (let number = startPage; number <= endPage; number++) {
      items.push(
        <Pagination.Item
          key={number}
          active={number === currentPage}
          onClick={() => handlePageChange(number)}
        >
          {number}
        </Pagination.Item>,
      );
    }

    if (endPage < totalPages) {
      if (endPage < totalPages - 1)
        items.push(<Pagination.Ellipsis key="ell-end" disabled />);

      items.push(
        <Pagination.Item
          key={totalPages}
          onClick={() => handlePageChange(totalPages)}
        >
          {totalPages}
        </Pagination.Item>,
      );
    }

    items.push(
      <Pagination.Next
        key="next"
        disabled={currentPage === totalPages}
        onClick={() => handlePageChange(currentPage + 1)}
      />,
    );

    items.push(
      <Pagination.Last
        key="last"
        disabled={currentPage === totalPages}
        onClick={() => handlePageChange(totalPages)}
      />,
    );

    return (
      <Pagination className="justify-content-center mt-4 flex-wrap gap-1">
        {items}
      </Pagination>
    );
  };

  return (
    <Container className="mt-4 pb-5">
      {/* === ШАПКА С КНОПКОЙ ДОБАВЛЕНИЯ === */}
      <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <h2 className="mb-0">Справочник добавок</h2>

        {canEdit && (
          <Button
            variant="success"
            className="fw-bold rounded-pill shadow-sm px-4"
            onClick={() => navigate("/additives/create")}
          >
            + Добавить добавку
          </Button>
        )}
      </div>

      <AdditivesFilter />

      {loading && !data ? (
        <div className="text-center mt-5">
          <Spinner animation="border" variant="success" />
        </div>
      ) : data?.values.length === 0 ? (
        <div className="text-center mt-5 text-muted">
          <h4>По вашему запросу ничего не найдено</h4>
          <p>Попробуйте изменить параметры фильтрации.</p>
        </div>
      ) : (
        <>
          <Row className="g-4 mb-4 align-items-stretch">
            {data?.values.map((additive) => (
              <Col key={additive.id} xs={12} sm={6} md={4} lg={3}>
                <div className="position-relative h-100">
                  {/* === КНОПКА РЕДАКТИРОВАНИЯ === */}
                  {canEdit && (
                    <Button
                      variant="light"
                      size="sm"
                      title="Редактировать добавку"
                      className="position-absolute shadow-sm d-flex align-items-center justify-content-center"
                      style={{
                        top: "10px",
                        right: "10px", // Разместили справа, чтобы не перекрывать важную инфу карточки
                        zIndex: 10,
                        width: "32px",
                        height: "32px",
                        borderRadius: "50%",
                        padding: 0,
                      }}
                      onClick={(e) => {
                        e.stopPropagation();
                        navigate(`/additives/${additive.id}/edit`);
                      }}
                    >
                      ✏️
                    </Button>
                  )}
                  <AdditiveCard
                    additive={additive}
                    onClick={(id) => handleCardClick(id)}
                  />
                </div>
              </Col>
            ))}
          </Row>

          {renderPagination()}
        </>
      )}
    </Container>
  );
};

export default AdditivesPage;
