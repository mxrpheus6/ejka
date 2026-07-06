import React, { useState, useEffect } from "react";
import {
  Container,
  Form,
  Button,
  Row,
  Col,
  Card,
  Spinner,
  Toast,
  ToastContainer,
} from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import type { RootState } from "../store/store";

import { additivesApi } from "../api/additivesApi";
import type { AdditiveRequest, DangerLevel, Origin } from "../types/additives";
import { useAuthRole } from "../hooks/useAuthRole";

import { useDispatch } from "react-redux";
import { clearAdditivesCache } from "../store/additivesSlice";

// === КОНСТАНТЫ ===
export const ORIGIN_TRANSLATIONS: Record<string, string> = {
  PLANT: "Растительное",
  MICROBIOLOGICAL: "Микробиологическое",
  SYNTHETIC: "Синтетическое",
  ANIMAL: "Животное",
  ARTIFICIAL: "Искусственное",
  MINERAL: "Минеральное",
};

export const DANGER_LEVEL_TRANSLATIONS: Record<string, string> = {
  SAFE: "Безопасная",
  WARNING: "Вызывает подозрения",
  DANGEROUS: "Опасная",
  BANNED: "Запрещена в РБ",
};

export const CATEGORIES_LIST = [
  "краситель",
  "консервант",
  "антиоксидант",
  "стабилизатор/эмульгатор",
  "усилитель вкуса",
  "антифламинги",
  "подсластители",
];

const CODE_REGEX = /^[eEеЕ]\s*\d{3,4}[a-zA-Zа-яА-Я]?$/;

const AddAdditivePage: React.FC = () => {
  const navigate = useNavigate();
  const isAuthenticated = useSelector(
    (state: RootState) => state.auth.isAuthenticated,
  );

  // === ПРОВЕРКА РОЛЕЙ ===
  const { isModerator, role } = useAuthRole();
  const canEdit = isModerator || role === "ROLE_ADMIN";

  // === СТЕЙТ ФОРМЫ ===
  const [code, setCode] = useState("");
  const [codeError, setCodeError] = useState("");
  const [nameRu, setNameRu] = useState("");
  const [nameEn, setNameEn] = useState("");
  const [category, setCategory] = useState("");
  const [dangerLevel, setDangerLevel] = useState<DangerLevel | "">("");
  const [warningDescription, setWarningDescription] = useState("");
  const [description, setDescription] = useState("");
  const [selectedOriginIds, setSelectedOriginIds] = useState<Set<number>>(
    new Set(),
  );

  // === СТЕЙТ ЗАГРУЗКИ ===
  const [origins, setOrigins] = useState<Origin[]>([]);
  const [isLoadingOrigins, setIsLoadingOrigins] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // === СТЕЙТ ДЛЯ ТОСТА ===
  const [toast, setToast] = useState({
    show: false,
    message: "",
    variant: "success",
  });

  const showToast = (message: string, variant: "success" | "danger") => {
    setToast({ show: true, message, variant });
  };

  const dispatch = useDispatch();

  useEffect(() => {
    const fetchOrigins = async () => {
      try {
        const data = await additivesApi.getOrigins();
        setOrigins(data);
      } catch (error) {
        console.error("Ошибка при загрузке происхождений добавки:", error);
      } finally {
        setIsLoadingOrigins(false);
      }
    };

    // Загружаем данные только если есть права
    if (isAuthenticated && canEdit) {
      fetchOrigins();
    } else {
      setIsLoadingOrigins(false);
    }
  }, [isAuthenticated, canEdit]);

  const toggleOrigin = (id: number) => {
    const newSet = new Set(selectedOriginIds);
    if (newSet.has(id)) {
      newSet.delete(id);
    } else {
      newSet.add(id);
    }
    setSelectedOriginIds(newSet);
  };

  const handleCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCode(e.target.value);
    if (codeError) setCodeError("");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!CODE_REGEX.test(code.trim())) {
      setCodeError("Неверный формат. Ожидается, например: E300 или E120i");
      return;
    }

    setIsSubmitting(true);

    try {
      const requestData: AdditiveRequest = {
        code: code.trim(),
        nameRu,
        nameEn,
        category,
        dangerLevel:
          dangerLevel !== "" ? (dangerLevel as DangerLevel) : undefined,
        warningDescription,
        description,
        originIds: Array.from(selectedOriginIds),
      };

      await additivesApi.createAdditive(requestData);

      dispatch(clearAdditivesCache());

      showToast(
        `Добавка ${code.trim().toUpperCase()} успешно создана!`,
        "success",
      );

      setTimeout(() => {
        navigate("/additives");
      }, 1500);
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || error.message;
      showToast("Ошибка сервера: " + errorMsg, "danger");
    } finally {
      setIsSubmitting(false);
    }
  };

  // === БЛОКИРОВКА ДОСТУПА ===
  if (!isAuthenticated || !canEdit) {
    return (
      <Container className="mt-5 pt-5 text-center pb-5">
        <div style={{ fontSize: "5rem", marginBottom: "20px" }}>🛑</div>
        <h2 className="fw-bold text-dark mb-3">Доступ ограничен</h2>
        <p className="text-muted mb-4" style={{ fontSize: "1.1rem" }}>
          Добавление новых добавок доступно только модераторам.
        </p>
        <Button
          variant="success"
          size="lg"
          className="rounded-pill px-4"
          onClick={() => navigate("/additives")}
        >
          Вернуться в справочник добавок
        </Button>
      </Container>
    );
  }

  return (
    <>
      <Container className="mt-4 pb-5">
        <h2 className="mb-4 fw-bold">Добавление новой добавки</h2>

        <Form onSubmit={handleSubmit} noValidate>
          {/* === ШАГ 1: БАЗОВАЯ ИНФОРМАЦИЯ === */}
          <Card className="mb-4 shadow-sm border-0 rounded-4">
            <Card.Body className="p-4">
              <h5 className="mb-4 text-success fw-bold">
                1. Основная информация
              </h5>
              <Row className="g-4">
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Код (E-номер) *
                    </Form.Label>
                    <Form.Control
                      required
                      maxLength={30}
                      value={code}
                      onChange={handleCodeChange}
                      isInvalid={!!codeError}
                      placeholder="Например: E300"
                      style={{ borderRadius: "10px" }}
                    />
                    <Form.Control.Feedback type="invalid">
                      {codeError}
                    </Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Категория
                    </Form.Label>
                    <Form.Select
                      value={category}
                      onChange={(e) => setCategory(e.target.value)}
                      style={{ borderRadius: "10px" }}
                    >
                      <option value="">Выберите категорию...</option>
                      {CATEGORIES_LIST.map((cat) => (
                        <option key={cat} value={cat}>
                          {cat.charAt(0).toUpperCase() + cat.slice(1)}
                        </option>
                      ))}
                    </Form.Select>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Название (RU)
                    </Form.Label>
                    <Form.Control
                      maxLength={100}
                      value={nameRu}
                      onChange={(e) => setNameRu(e.target.value)}
                      placeholder="Например: Аскорбиновая кислота"
                      style={{ borderRadius: "10px" }}
                    />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Название (EN)
                    </Form.Label>
                    <Form.Control
                      maxLength={100}
                      value={nameEn}
                      onChange={(e) => setNameEn(e.target.value)}
                      placeholder="Например: Ascorbic acid"
                      style={{ borderRadius: "10px" }}
                    />
                  </Form.Group>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          {/* === ШАГ 2: БЕЗОПАСНОСТЬ И ОПИСАНИЕ === */}
          <Card className="mb-4 shadow-sm border-0 rounded-4">
            <Card.Body className="p-4">
              <h5 className="mb-4 fw-bold">2. Характеристики безопасности</h5>
              <Row className="g-4">
                <Col md={12}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Уровень опасности
                    </Form.Label>
                    <Form.Select
                      value={dangerLevel}
                      onChange={(e) =>
                        setDangerLevel(e.target.value as DangerLevel)
                      }
                      style={{ borderRadius: "10px" }}
                    >
                      <option value="">Выберите уровень...</option>
                      {Object.entries(DANGER_LEVEL_TRANSLATIONS).map(
                        ([key, label]) => (
                          <option key={key} value={key}>
                            {label}
                          </option>
                        ),
                      )}
                    </Form.Select>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Общее описание
                    </Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={4}
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      placeholder="Описание свойств и применения добавки..."
                      style={{ borderRadius: "10px" }}
                    />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Предупреждения (Побочные эффекты)
                    </Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={4}
                      value={warningDescription}
                      onChange={(e) => setWarningDescription(e.target.value)}
                      placeholder="Влияние на здоровье, аллергические реакции..."
                      style={{ borderRadius: "10px" }}
                    />
                  </Form.Group>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          {/* === ШАГ 3: ПРОИСХОЖДЕНИЕ === */}
          <Card className="mb-5 shadow-sm border-0 rounded-4">
            <Card.Body className="p-4">
              <h5 className="mb-4 fw-bold">3. Происхождение (Origins)</h5>
              {isLoadingOrigins ? (
                <div className="text-center py-4">
                  <Spinner animation="border" variant="success" />
                </div>
              ) : origins.length > 0 ? (
                <div className="d-flex flex-wrap gap-3">
                  {origins.map((origin) => (
                    <Form.Check
                      key={origin.id}
                      type="checkbox"
                      id={`origin-${origin.id}`}
                      label={ORIGIN_TRANSLATIONS[origin.type] || origin.type}
                      checked={selectedOriginIds.has(origin.id)}
                      onChange={() => toggleOrigin(origin.id)}
                      className="px-3 py-2 border rounded-3 bg-light"
                      style={{ cursor: "pointer", borderRadius: "10px" }}
                    />
                  ))}
                </div>
              ) : (
                <p className="text-muted mb-0">Источники не найдены</p>
              )}
            </Card.Body>
          </Card>

          {/* === КНОПКА ОТПРАВКИ === */}
          <div className="d-grid gap-2 mb-5">
            <Button
              variant="success"
              size="lg"
              type="submit"
              disabled={isSubmitting}
              className="fw-bold py-3 shadow rounded-pill"
            >
              {isSubmitting ? (
                <>
                  <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    role="status"
                    aria-hidden="true"
                    className="me-2"
                  />{" "}
                  Сохранение...
                </>
              ) : (
                "Сохранить добавку"
              )}
            </Button>
          </div>
        </Form>
      </Container>

      {/* === КОНТЕЙНЕР ДЛЯ ТОСТА === */}
      <ToastContainer
        position="top-end"
        className="p-4"
        style={{ position: "fixed", zIndex: 1050 }}
      >
        <Toast
          onClose={() => setToast({ ...toast, show: false })}
          show={toast.show}
          delay={4000}
          autohide
          bg={toast.variant}
        >
          <Toast.Header closeButton={true}>
            <strong className="me-auto fw-bold text-dark">
              {toast.variant === "success" ? "✅ Успешно" : "❌ Ошибка"}
            </strong>
          </Toast.Header>
          <Toast.Body className="text-white fw-medium">
            {toast.message}
          </Toast.Body>
        </Toast>
      </ToastContainer>
    </>
  );
};

export default AddAdditivePage;
