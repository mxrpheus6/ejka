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
  Modal,
} from "react-bootstrap";
import { useNavigate, useParams } from "react-router-dom";
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

const EditAdditivePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
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
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

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
    const fetchData = async () => {
      if (!id) return;
      try {
        const [additiveData, originsData] = await Promise.all([
          additivesApi.getAdditiveById(id),
          additivesApi.getOrigins(),
        ]);

        setCode(additiveData.code);
        setNameRu(additiveData.nameRu || "");
        setNameEn(additiveData.nameEn || "");
        setCategory(additiveData.category || "");
        setDangerLevel(additiveData.dangerLevel || "");
        setWarningDescription(additiveData.warningDescription || "");
        setDescription(additiveData.description || "");
        setSelectedOriginIds(new Set(additiveData.origins.map((o) => o.id)));

        setOrigins(originsData);
      } catch (error) {
        showToast("Ошибка при загрузке данных", "danger");
      } finally {
        setIsLoading(false);
      }
    };

    // Загружаем данные только если есть права
    if (isAuthenticated && canEdit) {
      fetchData();
    } else {
      setIsLoading(false);
    }
  }, [id, isAuthenticated, canEdit]);

  const toggleOrigin = (id: number) => {
    const newSet = new Set(selectedOriginIds);
    if (newSet.has(id)) newSet.delete(id);
    else newSet.add(id);
    setSelectedOriginIds(newSet);
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

      await additivesApi.updateAdditive(id!, requestData);

      dispatch(clearAdditivesCache());

      showToast("Данные успешно обновлены!", "success");
    } catch (error: any) {
      showToast(
        "Ошибка при обновлении: " +
          (error.response?.data?.message || error.message),
        "danger",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    try {
      await additivesApi.deleteAdditive(id!);

      dispatch(clearAdditivesCache());

      showToast("Добавка удалена", "success");
      setTimeout(() => navigate("/additives"), 1000);
    } catch (error: any) {
      showToast("Не удалось удалить добавку", "danger");
    } finally {
      setShowDeleteModal(false);
    }
  };

  // === БЛОКИРОВКА ДОСТУПА ===
  if (!isAuthenticated || !canEdit) {
    return (
      <Container className="mt-5 pt-5 text-center pb-5">
        <div style={{ fontSize: "5rem", marginBottom: "20px" }}>🛑</div>
        <h2 className="fw-bold text-dark mb-3">Доступ ограничен</h2>
        <p className="text-muted mb-4" style={{ fontSize: "1.1rem" }}>
          Редактирование добавок доступно только модераторам.
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

  if (isLoading) {
    return (
      <Container className="mt-5 text-center">
        <Spinner animation="border" variant="success" />
      </Container>
    );
  }

  return (
    <>
      <Container className="mt-4 pb-5">
        <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
          <h2 className="fw-bold m-0">Редактирование добавки</h2>
          <Button
            variant="outline-danger"
            className="rounded-pill px-4 fw-bold"
            onClick={() => setShowDeleteModal(true)}
          >
            🗑️ Удалить
          </Button>
        </div>

        <Form onSubmit={handleSubmit} noValidate>
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
                      value={code}
                      onChange={(e) => {
                        setCode(e.target.value);
                        setCodeError("");
                      }}
                      isInvalid={!!codeError}
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
                      value={nameRu}
                      onChange={(e) => setNameRu(e.target.value)}
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
                      value={nameEn}
                      onChange={(e) => setNameEn(e.target.value)}
                      style={{ borderRadius: "10px" }}
                    />
                  </Form.Group>
                </Col>
              </Row>
            </Card.Body>
          </Card>

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
                      style={{ borderRadius: "10px" }}
                    />
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Предупреждения
                    </Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={4}
                      value={warningDescription}
                      onChange={(e) => setWarningDescription(e.target.value)}
                      style={{ borderRadius: "10px" }}
                    />
                  </Form.Group>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          <Card className="mb-5 shadow-sm border-0 rounded-4">
            <Card.Body className="p-4">
              <h5 className="mb-4 fw-bold">3. Происхождение (Origins)</h5>
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
            </Card.Body>
          </Card>

          <div className="d-grid gap-2 mb-5">
            <Button
              variant="success"
              size="lg"
              type="submit"
              disabled={isSubmitting}
              className="fw-bold py-3 shadow rounded-pill"
            >
              {isSubmitting ? <Spinner size="sm" /> : "Сохранить изменения"}
            </Button>
          </div>
        </Form>
      </Container>

      {/* === МОДАЛКА УДАЛЕНИЯ === */}
      <Modal
        show={showDeleteModal}
        onHide={() => setShowDeleteModal(false)}
        centered
      >
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Подтверждение удаления</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Вы уверены, что хотите удалить добавку? Это действие нельзя будет
          отменить.
        </Modal.Body>
        <Modal.Footer className="border-0">
          <Button
            variant="light"
            className="rounded-pill px-4"
            onClick={() => setShowDeleteModal(false)}
          >
            Отмена
          </Button>
          <Button
            variant="danger"
            className="rounded-pill px-4 fw-bold"
            onClick={handleDelete}
          >
            Да, удалить
          </Button>
        </Modal.Footer>
      </Modal>

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
          <Toast.Header>
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

export default EditAdditivePage;
