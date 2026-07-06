import React, { useState, useRef, useEffect } from "react";
import {
  Container,
  Form,
  Button,
  Row,
  Col,
  Card,
  Spinner,
  Alert,
  Modal,
  Toast,
  ToastContainer,
} from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { scansApi } from "../api/scansApi";
import { productsApi } from "../api/productsApi";
import type { ScanResponse } from "../types/scan";
import type { ProductCategory, ProductRequest } from "../types/products";
import { ProductCategoryLabels } from "../types/products";
import styles from "./ModificationProductPage.module.css";
import AdditiveCard from "../components/AdditiveCard";
import { additivesApi } from "../api/additivesApi";
import type { RootState } from "../store/store";
import { useDispatch, useSelector } from "react-redux";

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

const AddProductPage: React.FC = () => {
  const navigate = useNavigate();

  const isAuthenticated = useSelector(
    (state: RootState) => state.auth.isAuthenticated,
  );

  // === СТЕЙТ ФОРМЫ (ТЕКСТОВЫЕ ДАННЫЕ) ===
  const [title, setTitle] = useState("");
  const [barcode, setBarcode] = useState("");
  const [category, setCategory] = useState<ProductCategory>("GENERAL");
  const [calories, setCalories] = useState<number | "">("");
  const [proteins, setProteins] = useState<number | "">("");
  const [fats, setFats] = useState<number | "">("");
  const [carbs, setCarbs] = useState<number | "">("");

  // === СТЕЙТ ОШИБОК ВАЛИДАЦИИ ФОРМЫ ===
  const [errors, setErrors] = useState<Record<string, string>>({});

  // === СТЕЙТ ФАЙЛОВ ===
  const [mainFile, setMainFile] = useState<File | null>(null);
  const [barcodeFile, setBarcodeFile] = useState<File | null>(null);
  const [ingredientsFile, setIngredientsFile] = useState<File | null>(null);

  const [ingredientsPreview, setIngredientsPreview] = useState<string | null>(
    null,
  );
  const [mainPreview, setMainPreview] = useState<string | null>(null);
  const [barcodePreview, setBarcodePreview] = useState<string | null>(null);

  // === СТЕЙТ АНАЛИЗА (РЕДАКТИРУЕМЫЙ) ===
  const [isScanning, setIsScanning] = useState(false);
  const [scanError, setScanError] = useState<string | null>(null);
  const [compositionText, setCompositionText] = useState("");
  const [hasPalmOil, setHasPalmOil] = useState(false);
  const [parsedAdditives, setParsedAdditives] = useState<any[]>([]);
  const [parsedAllergens, setParsedAllergens] = useState<string[]>([]);

  // === ЛИМИТЫ ===
  const [showLimitModal, setShowLimitModal] = useState(false);
  const [isLimitReached, setIsLimitReached] = useState(false);

  // === РЕФЫ ДЛЯ СКРЫТЫХ ИНПУТОВ ===
  const ingredientsInputRef = useRef<HTMLInputElement>(null);
  const mainInputRef = useRef<HTMLInputElement>(null);
  const barcodeInputRef = useRef<HTMLInputElement>(null);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [newAllergen, setNewAllergen] = useState<string>("");
  const [newAdditiveCode, setNewAdditiveCode] = useState<string>("");

  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);

  // === СТЕЙТ ДЛЯ ТОСТА ===
  const [toast, setToast] = useState({
    show: false,
    message: "",
    variant: "success",
  });

  const showToast = (
    message: string,
    variant: "success" | "danger" | "warning",
  ) => {
    setToast({ show: true, message, variant });
  };

  useEffect(() => {
    const timer = setTimeout(async () => {
      if (searchQuery.trim().length >= 2) {
        setIsSearching(true);
        try {
          const response = await additivesApi.searchAdditives(searchQuery);
          const results = (response as any).values || [];
          setSearchResults(results);
          setShowDropdown(results.length > 0);
        } catch (err) {
          console.error("Ошибка поиска", err);
          setSearchResults([]);
        } finally {
          setIsSearching(false);
        }
      } else {
        setSearchResults([]);
        setShowDropdown(false);
      }
    }, 700);

    return () => clearTimeout(timer);
  }, [searchQuery]);

  const handleSelectSearchedAdditive = (additive: any) => {
    if (!parsedAdditives.some((a) => a.id === additive.id)) {
      setParsedAdditives([...parsedAdditives, additive]);
    }
    setSearchQuery("");
    setShowDropdown(false);
  };

  const handleIngredientsFileChange = (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];
      setIngredientsFile(file);
      setIngredientsPreview(URL.createObjectURL(file));
      setScanError(null);
    }
  };

  const handleFileChange = (
    e: React.ChangeEvent<HTMLInputElement>,
    setFile: React.Dispatch<React.SetStateAction<File | null>>,
    setPreview: React.Dispatch<React.SetStateAction<string | null>>,
  ) => {
    if (e.target.files && e.target.files.length > 0) {
      const file = e.target.files[0];
      setFile(file);
      setPreview(URL.createObjectURL(file));
    }
  };

  const handleStartScanClick = async () => {
    if (!ingredientsFile) return;
    setIsScanning(true);
    setScanError(null);
    try {
      const scanId = await scansApi.analyze(ingredientsFile);
      pollForResult(scanId);
    } catch (err: any) {
      setIsScanning(false);
      if (err.response?.status === 403) {
        setShowLimitModal(true);
        setIsLimitReached(true);
        setScanError(
          "Лимит автоматических сканирований исчерпан. Вы можете заполнить состав вручную.",
        );
      } else {
        setScanError(
          "Не удалось запустить анализ. Проверьте соединение или попробуйте другое фото.",
        );
      }
    }
  };

  const pollForResult = (scanId: string) => {
    let attempts = 0;
    const interval = setInterval(async () => {
      attempts++;
      try {
        const res = await scansApi.getResult(scanId);
        if (res.status === 200 && res.data) {
          clearInterval(interval);
          const data = res.data as ScanResponse;

          if (data.status === "SUCCESS") {
            setCompositionText(data.parsedText || "");
            setHasPalmOil((data.controversial?.length ?? 0) > 0);
            if (data.additives && data.additives.length > 0) {
              try {
                const additiveIds = data.additives.map((a) => a.id);
                const fullAdditives =
                  await additivesApi.getAdditivesBatch(additiveIds);
                setParsedAdditives(fullAdditives);
              } catch (err) {
                setParsedAdditives(data.additives);
              }
            } else {
              setParsedAdditives([]);
            }
            const allergensList = (data.allergens || []).map((a) => a.category);
            setParsedAllergens(allergensList);
          } else {
            setScanError(
              data.errorMessage || "Ошибка при распознавании текста.",
            );
          }
          setIsScanning(false);
        } else if (attempts >= 30) {
          clearInterval(interval);
          setScanError("Превышено время ожидания ответа сервера.");
          setIsScanning(false);
        }
      } catch (err) {
        clearInterval(interval);
        setScanError("Потеряно соединение с сервером.");
        setIsScanning(false);
      }
    }, 2000);
  };

  const removeAdditive = (idToRemove: number) => {
    setParsedAdditives((prev) => prev.filter((a) => a.id !== idToRemove));
  };

  const removeAllergen = (allergenToRemove: string) => {
    setParsedAllergens((prev) => prev.filter((a) => a !== allergenToRemove));
  };

  const handleAddAllergen = () => {
    if (newAllergen && !parsedAllergens.includes(newAllergen)) {
      setParsedAllergens([...parsedAllergens, newAllergen]);
    }
    setNewAllergen("");
  };

  const handleAddAdditive = async () => {
    const code = newAdditiveCode.trim();
    if (!code) return;
    try {
      const formattedCode = code.toUpperCase();
      const additive = await additivesApi.getAdditiveByCode(formattedCode);
      if (!parsedAdditives.some((a) => a.id === additive.id)) {
        setParsedAdditives([...parsedAdditives, additive]);
      }
      setNewAdditiveCode("");
    } catch (err) {
      showToast(
        `Добавка с кодом ${code} не найдена в базе. Проверьте правильность написания.`,
        "warning",
      );
    }
  };

  // === ЛОГИКА ВАЛИДАЦИИ ФОРМЫ ===
  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    if (!barcode.trim()) {
      newErrors.barcode = "Штрихкод обязателен";
    } else if (!/^\d+$/.test(barcode.trim())) {
      newErrors.barcode = "Штрихкод должен содержать только цифры";
    }

    if (!title.trim()) {
      newErrors.title = "Название обязательно";
    }

    if (calories === "" || Number(calories) < 0 || Number(calories) > 900) {
      newErrors.calories = "От 0 до 900";
    } else if (!Number.isInteger(Number(calories))) {
      newErrors.calories = "Должно быть целым";
    }

    if (proteins === "" || Number(proteins) < 0 || Number(proteins) > 100) {
      newErrors.proteins = "От 0 до 100";
    }
    if (fats === "" || Number(fats) < 0 || Number(fats) > 100) {
      newErrors.fats = "От 0 до 100";
    }
    if (carbs === "" || Number(carbs) < 0 || Number(carbs) > 100) {
      newErrors.carbs = "От 0 до 100";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const clearError = (field: string) => {
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      showToast("Пожалуйста, исправьте ошибки в форме", "danger");
      return;
    }

    setIsSubmitting(true);
    try {
      const requestData: ProductRequest = {
        barcode: barcode.trim(),
        title: title.trim(),
        category,
        calories: Number(calories),
        proteins: Number(proteins),
        fats: Number(fats),
        carbohydrates: Number(carbs),
        additiveIds: parsedAdditives.map((a) => a.id),
        allergens: parsedAllergens,
        hasPalmOil,
        compositionText,
      };

      const newProduct = await productsApi.createProduct(
        requestData,
        mainFile,
        ingredientsFile,
        barcodeFile,
      );

      showToast("Продукт успешно отправлен!", "success");
      setTimeout(
        () => navigate(`/products/${newProduct.id}`, { replace: true }),
        1000,
      );
    } catch (error: any) {
      const status = error.response?.status;
      const errorMsg = error.response?.data?.message || error.message;

      if (status === 409) {
        showToast(errorMsg || "Этот продукт уже был добавлен.", "warning");
      } else if (status === 429) {
        showToast(
          errorMsg ||
            "Слишком много товаров на модерации. Пожалуйста, подождите.",
          "danger",
        );
      } else {
        showToast("Ошибка при сохранении продукта: " + errorMsg, "danger");
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <Container className="mt-5 pt-5 text-center pb-5">
        <div style={{ fontSize: "5rem", marginBottom: "20px" }}>🛑</div>
        <h2 className="fw-bold text-dark mb-3">Доступ ограничен</h2>
        <p className="text-muted mb-4" style={{ fontSize: "1.1rem" }}>
          Добавление новых продуктов в базу доступно только авторизованным
          пользователям.
        </p>
        <Button
          variant="success"
          size="lg"
          className="rounded-pill px-4"
          onClick={() => navigate("/products")}
        >
          Вернуться в каталог продуктов
        </Button>
      </Container>
    );
  }

  return (
    <>
      <Container className="mt-4 pb-5">
        <h2 className="mb-4 fw-bold">Добавление нового продукта</h2>

        <Form onSubmit={handleSubmit} noValidate>
          {/* === ШАГ 1: АНАЛИЗ СОСТАВА === */}
          <Card className="mb-4 shadow-sm border-0 rounded-4">
            <Card.Body className="p-4">
              <h5 className="mb-4 text-success fw-bold">1. Состав и добавки</h5>
              <p className="text-muted small mb-4">
                Загрузите фото состава и запустите сканирование, либо заполните
                данные вручную.
              </p>

              <input
                type="file"
                accept="image/*"
                className={styles.fileInput}
                ref={ingredientsInputRef}
                onChange={handleIngredientsFileChange}
              />

              <Row className="g-4 align-items-stretch">
                <Col lg={5} md={12} className="d-flex flex-column">
                  {!ingredientsPreview ? (
                    <div
                      className={`${styles.uploadBox} flex-grow-1 mb-3`}
                      onClick={() => ingredientsInputRef.current?.click()}
                    >
                      <div className={styles.uploadIcon}>📸</div>
                      <h6 className="mt-2 text-center">
                        Загрузить фото состава
                      </h6>
                    </div>
                  ) : (
                    <div
                      className={`${styles.previewContainer} flex-grow-1 mb-3`}
                      onClick={() => ingredientsInputRef.current?.click()}
                    >
                      <img
                        src={ingredientsPreview}
                        alt="Состав"
                        className={styles.previewImage}
                      />
                      <div className={styles.replaceOverlay}>
                        ✏️ Заменить фото
                      </div>
                    </div>
                  )}

                  <div className="d-grid mt-auto">
                    <Button
                      variant="success"
                      className="fw-bold shadow-sm rounded-pill py-2"
                      disabled={
                        !ingredientsFile || isScanning || isLimitReached
                      }
                      onClick={handleStartScanClick}
                    >
                      ✨ Сканировать
                    </Button>
                  </div>
                </Col>

                <Col lg={7} md={12}>
                  {scanError && (
                    <Alert
                      variant={isLimitReached ? "warning" : "danger"}
                      className="mb-3 rounded-4"
                    >
                      <div className="fw-bold mb-1">Внимание</div>
                      <div>{scanError}</div>
                    </Alert>
                  )}

                  {isScanning ? (
                    <div className="d-flex flex-column align-items-center justify-content-center h-100 py-5 bg-light rounded-4 border">
                      <Spinner animation="border" variant="success" />
                      <p className="mt-3 text-muted fw-bold">
                        Анализируем текст с картинки...
                      </p>
                    </div>
                  ) : (
                    <div className="p-4 bg-light rounded-4 shadow-sm border h-100">
                      <Form.Group className="mb-4">
                        <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                          Текст состава (можно ввести вручную)
                        </Form.Label>
                        <Form.Control
                          as="textarea"
                          rows={4}
                          value={compositionText}
                          placeholder="Мука пшеничная, сахар, молоко..."
                          onChange={(e) => setCompositionText(e.target.value)}
                          style={{ fontSize: "0.9rem", borderRadius: "12px" }}
                        />
                      </Form.Group>

                      {/* Добавки */}
                      <div className="mb-4 p-3 bg-white rounded-4 border">
                        <span className="fw-bold text-dark d-block mb-3">
                          Добавки ({parsedAdditives.length})
                        </span>
                        {parsedAdditives.length > 0 && (
                          <Row className="g-2 mb-3">
                            {parsedAdditives.map((add) => (
                              <Col key={add.id} xs={12} sm={6}>
                                <div className="position-relative h-100">
                                  <AdditiveCard
                                    additive={add}
                                    onClick={() => {}}
                                  />
                                  <Button
                                    variant="danger"
                                    size="sm"
                                    className="position-absolute shadow-sm"
                                    style={{
                                      top: "-6px",
                                      right: "-6px",
                                      width: "24px",
                                      height: "24px",
                                      padding: 0,
                                      borderRadius: "50%",
                                      zIndex: 10,
                                    }}
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      removeAdditive(add.id);
                                    }}
                                  >
                                    ✕
                                  </Button>
                                </div>
                              </Col>
                            ))}
                          </Row>
                        )}

                        <div className="position-relative">
                          <Form.Control
                            size="sm"
                            type="text"
                            placeholder="Поиск добавки (E300...)"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onFocus={() =>
                              searchResults.length > 0 && setShowDropdown(true)
                            }
                            onBlur={() =>
                              setTimeout(() => setShowDropdown(false), 200)
                            }
                            style={{ borderRadius: "8px" }}
                          />
                          {showDropdown && (
                            <div
                              className="position-absolute w-100 bg-white border rounded shadow-sm mt-1"
                              style={{
                                zIndex: 1050,
                                maxHeight: "200px",
                                overflowY: "auto",
                              }}
                            >
                              {searchResults.map((add) => (
                                <div
                                  key={add.id}
                                  className="p-2 border-bottom"
                                  style={{
                                    cursor: "pointer",
                                    fontSize: "0.85rem",
                                  }}
                                  onMouseDown={(e) => {
                                    e.preventDefault();
                                    handleSelectSearchedAdditive(add);
                                  }}
                                >
                                  <span className="fw-bold me-2 text-success">
                                    {add.code}
                                  </span>
                                  {add.nameRu}
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Аллергены */}
                      <div className="mb-4 p-3 bg-white rounded-4 border">
                        <span className="fw-bold text-dark d-block mb-3">
                          Аллергены ({parsedAllergens.length})
                        </span>
                        {parsedAllergens.length > 0 && (
                          <div className="d-flex flex-wrap gap-2 mb-3">
                            {parsedAllergens.map((al) => (
                              <div
                                key={al}
                                className="d-flex align-items-center gap-2 p-2 border rounded-3 shadow-sm bg-white"
                                style={{ borderLeft: "4px solid #ef4444" }}
                              >
                                <span className="fs-5">
                                  {ALLERGEN_EMOJIS[al]}
                                </span>
                                <span className="fw-bold small">
                                  {ALLERGEN_NAMES_RU[al]}
                                </span>
                                <Button
                                  variant="link"
                                  className="text-danger p-0 text-decoration-none ms-1"
                                  onClick={() => removeAllergen(al)}
                                >
                                  ✕
                                </Button>
                              </div>
                            ))}
                          </div>
                        )}
                        <div className="d-flex gap-2 align-items-center">
                          <Form.Select
                            size="sm"
                            value={newAllergen}
                            onChange={(e) => setNewAllergen(e.target.value)}
                            style={{ borderRadius: "8px" }}
                          >
                            <option value="">Выберите аллерген...</option>
                            {Object.entries(ALLERGEN_NAMES_RU)
                              .filter(([key]) => !parsedAllergens.includes(key))
                              .map(([key, name]) => (
                                <option key={key} value={key}>
                                  {ALLERGEN_EMOJIS[key]} {name}
                                </option>
                              ))}
                          </Form.Select>
                          <Button
                            variant="outline-danger"
                            size="sm"
                            onClick={handleAddAllergen}
                            disabled={!newAllergen}
                            style={{ borderRadius: "8px" }}
                          >
                            Добавить
                          </Button>
                        </div>
                      </div>

                      {/* Пальмовое масло */}
                      <div className="p-3 bg-white rounded-4 border d-flex justify-content-between align-items-center">
                        <div className="d-flex align-items-center gap-2">
                          <span className="fs-4">🌴</span>
                          <span className="fw-bold text-dark">
                            Пальмовое масло
                          </span>
                        </div>
                        <Form.Check
                          type="switch"
                          checked={hasPalmOil}
                          onChange={(e) => setHasPalmOil(e.target.checked)}
                          style={{ transform: "scale(1.3)" }}
                        />
                      </div>
                    </div>
                  )}
                </Col>
              </Row>
            </Card.Body>
          </Card>

          {/* === ШАГ 2: БАЗОВАЯ ИНФОРМАЦИЯ === */}
          <Card className="mb-4 shadow-sm border-0 rounded-4">
            <Card.Body className="p-4">
              <h5 className="mb-4 fw-bold">2. Основная информация</h5>
              <Row className="g-4">
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Штрихкод *
                    </Form.Label>
                    <Form.Control
                      value={barcode}
                      onChange={(e) => {
                        setBarcode(e.target.value);
                        clearError("barcode");
                      }}
                      isInvalid={!!errors.barcode}
                      placeholder="Например: 4601234567890"
                      style={{ borderRadius: "10px" }}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.barcode}
                    </Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col md={6}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Название продукта *
                    </Form.Label>
                    <Form.Control
                      value={title}
                      onChange={(e) => {
                        setTitle(e.target.value);
                        clearError("title");
                      }}
                      isInvalid={!!errors.title}
                      placeholder="Например: Печенье протеиновое"
                      style={{ borderRadius: "10px" }}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.title}
                    </Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col md={12}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Категория *
                    </Form.Label>
                    <Form.Select
                      value={category}
                      onChange={(e) =>
                        setCategory(e.target.value as ProductCategory)
                      }
                      style={{ borderRadius: "10px" }}
                    >
                      {Object.entries(ProductCategoryLabels).map(
                        ([key, label]) => (
                          <option key={key} value={key}>
                            {label}
                          </option>
                        ),
                      )}
                    </Form.Select>
                  </Form.Group>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          {/* === ШАГ 3: КБЖУ === */}
          <Card className="mb-4 shadow-sm border-0 rounded-4">
            <Card.Body className="p-4">
              <h5 className="mb-4 fw-bold">3. Пищевая ценность (на 100г)</h5>
              <Row className="g-4">
                <Col xs={6} md={3}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Ккал *
                    </Form.Label>
                    <Form.Control
                      type="number"
                      min="0"
                      max="900"
                      value={calories}
                      onChange={(e) => {
                        setCalories(
                          e.target.value ? Number(e.target.value) : "",
                        );
                        clearError("calories");
                      }}
                      isInvalid={!!errors.calories}
                      style={{ borderRadius: "10px" }}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.calories}
                    </Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col xs={6} md={3}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Белки (г) *
                    </Form.Label>
                    <Form.Control
                      type="number"
                      step="0.1"
                      min="0"
                      max="100"
                      value={proteins}
                      onChange={(e) => {
                        setProteins(
                          e.target.value ? Number(e.target.value) : "",
                        );
                        clearError("proteins");
                      }}
                      isInvalid={!!errors.proteins}
                      style={{ borderRadius: "10px" }}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.proteins}
                    </Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col xs={6} md={3}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Жиры (г) *
                    </Form.Label>
                    <Form.Control
                      type="number"
                      step="0.1"
                      min="0"
                      max="100"
                      value={fats}
                      onChange={(e) => {
                        setFats(e.target.value ? Number(e.target.value) : "");
                        clearError("fats");
                      }}
                      isInvalid={!!errors.fats}
                      style={{ borderRadius: "10px" }}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.fats}
                    </Form.Control.Feedback>
                  </Form.Group>
                </Col>
                <Col xs={6} md={3}>
                  <Form.Group>
                    <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider">
                      Углеводы (г) *
                    </Form.Label>
                    <Form.Control
                      type="number"
                      step="0.1"
                      min="0"
                      max="100"
                      value={carbs}
                      onChange={(e) => {
                        setCarbs(e.target.value ? Number(e.target.value) : "");
                        clearError("carbs");
                      }}
                      isInvalid={!!errors.carbs}
                      style={{ borderRadius: "10px" }}
                    />
                    <Form.Control.Feedback type="invalid">
                      {errors.carbs}
                    </Form.Control.Feedback>
                  </Form.Group>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          {/* === ШАГ 4: ФОТОГРАФИИ === */}
          <Card className="mb-5 shadow-sm border-0 rounded-4">
            <Card.Body className="p-4">
              <h5 className="mb-4 fw-bold">4. Фотографии продукта</h5>

              <input
                type="file"
                accept="image/*"
                className={styles.fileInput}
                ref={mainInputRef}
                onChange={(e) =>
                  handleFileChange(e, setMainFile, setMainPreview)
                }
              />
              <input
                type="file"
                accept="image/*"
                className={styles.fileInput}
                ref={barcodeInputRef}
                onChange={(e) =>
                  handleFileChange(e, setBarcodeFile, setBarcodePreview)
                }
              />

              <Row className="g-4">
                <Col md={6}>
                  <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider mb-2 d-block">
                    Главное фото
                  </Form.Label>
                  {mainPreview ? (
                    <div
                      className={styles.previewContainer}
                      onClick={() => mainInputRef.current?.click()}
                    >
                      <img
                        src={mainPreview}
                        alt="Main"
                        className={styles.previewImage}
                      />
                      <div className={styles.replaceOverlay}>
                        ✏️ Заменить фото
                      </div>
                    </div>
                  ) : (
                    <div
                      className={styles.uploadBox}
                      onClick={() => mainInputRef.current?.click()}
                    >
                      <div className={styles.uploadIcon}>📦</div>
                      <h6 className="text-center m-0">Лицевая сторона</h6>
                    </div>
                  )}
                </Col>

                <Col md={6}>
                  <Form.Label className="small fw-bold text-secondary text-uppercase tracking-wider mb-2 d-block">
                    Дополнительное фото
                  </Form.Label>
                  {barcodePreview ? (
                    <div
                      className={styles.previewContainer}
                      onClick={() => barcodeInputRef.current?.click()}
                    >
                      <img
                        src={barcodePreview}
                        alt="Barcode"
                        className={styles.previewImage}
                      />
                      <div className={styles.replaceOverlay}>
                        ✏️ Заменить фото
                      </div>
                    </div>
                  ) : (
                    <div
                      className={styles.uploadBox}
                      onClick={() => barcodeInputRef.current?.click()}
                    >
                      <div className={styles.uploadIcon}>🏷️</div>
                      <h6 className="text-center m-0">Дополнительное фото</h6>
                    </div>
                  )}
                </Col>
              </Row>
            </Card.Body>
          </Card>

          <div className="d-grid gap-2 mb-5">
            <Button
              variant="success"
              size="lg"
              type="submit"
              disabled={isSubmitting || isScanning}
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
                "Отправить продукт"
              )}
            </Button>
          </div>
        </Form>

        {/* 👇 МОДАЛКА ЛИМИТОВ */}
        <Modal
          show={showLimitModal}
          onHide={() => setShowLimitModal(false)}
          centered
        >
          <Modal.Body className="text-center p-5">
            <div style={{ fontSize: "4.5rem", lineHeight: 1 }}>💎</div>
            <h3 className="mt-4 fw-bold">Лимит исчерпан</h3>
            <p className="text-muted mt-3 mb-4" style={{ fontSize: "1.05rem" }}>
              Вы использовали все доступные бесплатные сканирования по фото.
              Оформите Premium-подписку, чтобы получить{" "}
              <b>безлимитный доступ</b> к ИИ-сканеру Ежки!
            </p>
            <Button
              variant="success"
              size="lg"
              className="w-100 rounded-pill fw-bold py-3 mb-3 shadow-sm"
              style={{ backgroundColor: "#539155", border: "none" }}
              onClick={() => {
                setShowLimitModal(false);
                navigate("/subscription");
              }}
            >
              ✨ Перейти на Premium
            </Button>
            <Button
              variant="link"
              className="text-muted text-decoration-none"
              onClick={() => setShowLimitModal(false)}
            >
              Заполнить состав вручную
            </Button>
          </Modal.Body>
        </Modal>
      </Container>

      {/* === КОНТЕЙНЕР ДЛЯ ТОСТА (УВЕДОМЛЕНИЙ) === */}
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
              {toast.variant === "success" && "✅ Успешно"}
              {toast.variant === "warning" && "⚠️ Внимание"}
              {toast.variant === "danger" && "❌ Ошибка"}
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

export default AddProductPage;
