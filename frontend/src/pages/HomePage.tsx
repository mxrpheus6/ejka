import React, { useState, useRef, useEffect } from "react";
import {
  Container,
  Row,
  Col,
  Spinner,
  Alert,
  Button,
  Modal,
  Form,
  Toast,
  ToastContainer,
} from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { Html5Qrcode } from "html5-qrcode";
import type { RootState } from "../store/store";
import {
  setPreviewUrl,
  setScanResult,
  setDetailedAdditives,
  clearScan,
} from "../store/scanSlice";
import { scansApi } from "../api/scansApi";
import { additivesApi } from "../api/additivesApi";
import { productsApi } from "../api/productsApi";
import type { ScanResponse } from "../types/scan";
import styles from "./HomePage.module.css";
import AuthModal from "../components/AuthModal";
import { authApi } from "../api/authApi";
import { setUser } from "../store/authSlice";

// Импорты блоков
import AllergensBlock from "../components/blocks/AllergensBlock";
import ControversialBlock from "../components/blocks/ControversialBlock";
import AdditivesBlock from "../components/blocks/AdditivesBlock";
import MethodologyBlock from "../components/blocks/MethodologyBlock"; // <-- ДОБАВИЛИ ИМПОРТ

// КОНСТАНТЫ ДЛЯ ВАЛИДАЦИИ ФАЙЛОВ
const MAX_FILE_SIZE_MB = 20;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
const ALLOWED_TYPES = ["image/jpeg", "image/png", "image/heic", "image/webp"];

const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const { previewUrl, scanResult, detailedAdditives } = useSelector(
    (state: RootState) => state.scan,
  );

  const { isAuthenticated, user } = useSelector(
    (state: RootState) => state.auth,
  );

  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // СОСТОЯНИЯ ДЛЯ УВЕДОМЛЕНИЙ И МОДАЛОК
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [showAuthModal, setShowAuthModal] = useState<boolean>(false);
  const [showLimitModal, setShowLimitModal] = useState<boolean>(false);

  const [showBarcodeModal, setShowBarcodeModal] = useState<boolean>(false);
  const [barcodeError, setBarcodeError] = useState<string | null>(null);

  const [showManualInput, setShowManualInput] = useState<boolean>(false);
  const [manualBarcode, setManualBarcode] = useState<string>("");

  const [scannerKey, setScannerKey] = useState<number>(0);
  const [isDragging, setIsDragging] = useState<boolean>(false);

  useEffect(() => {
    let html5QrCode: Html5Qrcode | null = null;
    let timeoutId: ReturnType<typeof setTimeout>;

    if (showBarcodeModal) {
      html5QrCode = new Html5Qrcode("barcode-reader");

      const config = {
        fps: 10,
        qrbox: { width: 250, height: 150 },
        aspectRatio: 1.0,
      };

      html5QrCode
        .start(
          { facingMode: "environment" },
          config,
          async (decodedText) => {
            if (html5QrCode && html5QrCode.isScanning) {
              await html5QrCode.stop().catch(() => {});
              setShowManualInput(true);
              handleBarcodeScanned(decodedText);
            }
          },
          undefined,
        )
        .catch((err) => {
          setBarcodeError(`Ошибка камеры: ${err.message || err}`);
          setShowManualInput(true);
        });

      timeoutId = setTimeout(() => {
        setShowManualInput(true);
      }, 10000);
    }

    return () => {
      clearTimeout(timeoutId);
      if (html5QrCode && html5QrCode.isScanning) {
        html5QrCode.stop().catch(() => {});
      }
    };
  }, [showBarcodeModal, scannerKey]);

  const checkLimitsPreventively = (): boolean => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return false;
    }

    if (!user?.isPremium && (user?.scansCount || 0) >= 5) {
      setShowLimitModal(true);
      return false;
    }

    return true;
  };

  const handleBarcodeScanned = async (barcode: string) => {
    setLoading(true);
    setBarcodeError(null);
    try {
      const product = await productsApi.getProductByBarcode(barcode);
      setShowBarcodeModal(false);
      navigate(`/products/${product.id}`);
    } catch (err: any) {
      console.error("=== ОШИБКА ПРИ ПОИСКЕ ШТРИХКОДА ===", err);
      const errorMessage =
        err.response?.data?.message || err.message || "Продукт не найден";
      setBarcodeError(`Штрихкод ${barcode}: ${errorMessage}`);
    } finally {
      setLoading(false);
    }
  };

  const handleBoxClick = () => {
    if (!checkLimitsPreventively()) return;
    fileInputRef.current?.click();
  };

  const handleBarcodeBoxClick = () => {
    setBarcodeError(null);
    setShowManualInput(false);
    setManualBarcode("");
    setScannerKey((prev) => prev + 1);
    setShowBarcodeModal(true);
  };

  const processSelectedFile = async (file: File) => {
    if (!checkLimitsPreventively()) return;

    if (!ALLOWED_TYPES.includes(file.type)) {
      setToastMessage(
        "Неподдерживаемый формат файла. Допустимы только JPG, PNG или HEIC.",
      );
      return;
    }

    if (file.size > MAX_FILE_SIZE_BYTES) {
      setToastMessage(
        `Размер файла превышает лимит (${MAX_FILE_SIZE_MB} МБ). Пожалуйста, выберите файл меньшего размера.`,
      );
      return;
    }

    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }

    const newUrl = URL.createObjectURL(file);
    dispatch(setPreviewUrl(newUrl));
    setError(null);
    await startScan(file);
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      await processSelectedFile(e.target.files[0]);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleDragEnter = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isDragging) setIsDragging(true);
  };

  const handleDrop = async (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    if (!checkLimitsPreventively()) return;

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      await processSelectedFile(e.dataTransfer.files[0]);
    }
  };

  const startScan = async (file: File) => {
    setLoading(true);
    try {
      const scanId = await scansApi.analyze(file);

      if (isAuthenticated && !user?.isPremium) {
        authApi
          .getUserProfile()
          .then((profile) => dispatch(setUser(profile)))
          .catch(console.error);
      }

      pollForResult(scanId);
    } catch (err: any) {
      console.error("=== ОШИБКА ПРИ ОТПРАВКЕ ФОТО ===", err);

      const errorMessage =
        err.response?.data?.message || err.message || "Неизвестная ошибка";

      if (
        err.response?.status === 403 ||
        errorMessage.includes("403") ||
        errorMessage.includes("Лимит")
      ) {
        resetScanner();
        setShowLimitModal(true);
      } else {
        setError(`Ошибка: ${errorMessage}`);
      }
      setLoading(false);
    }
  };

  const pollForResult = (scanId: string) => {
    let attempts = 0;
    const maxAttempts = 30;

    const interval = setInterval(async () => {
      attempts++;
      try {
        const res = await scansApi.getResult(scanId);

        if (res.status === 200 && res.data) {
          clearInterval(interval);
          const responseData = res.data as ScanResponse;

          if (responseData.status === "SUCCESS") {
            dispatch(setScanResult(responseData));
            await loadDetailedAdditives(responseData);
          } else {
            const errMsg = responseData.errorMessage || "";
            if (errMsg.includes("403") || errMsg.includes("Лимит")) {
              resetScanner();
              setShowLimitModal(true);
            } else {
              setError(errMsg || "Ошибка при анализе изображения");
            }
          }
          setLoading(false);
        } else if (attempts >= maxAttempts) {
          clearInterval(interval);
          setError(
            "Превышено время ожидания. Возможно, фото слишком большое или сервер перегружен.",
          );
          setLoading(false);
        }
      } catch (err: any) {
        clearInterval(interval);
        setError("Потеряно соединение с сервером при ожидании результата");
        setLoading(false);
      }
    }, 2000);
  };

  const loadDetailedAdditives = async (data: ScanResponse) => {
    if (!data.additives || data.additives.length === 0) return;

    try {
      const promises = data.additives.map((a) =>
        additivesApi.getAdditiveById(a.id.toString()),
      );
      const results = await Promise.all(promises);
      dispatch(setDetailedAdditives(results));
    } catch (err) {
      console.error(
        "Не удалось загрузить детальную информацию о добавках",
        err,
      );
    }
  };

  const resetScanner = () => {
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
    dispatch(clearScan());
    setError(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  // Обернули всё в Fragment (<>), чтобы MethodologyBlock можно было разместить на всю ширину под Container
  return (
    <>
      <Container className="mt-4 pb-5">
        <ToastContainer
          position="top-end"
          className="p-3"
          style={{ position: "fixed", zIndex: 1050 }}
        >
          <Toast
            show={!!toastMessage}
            onClose={() => setToastMessage(null)}
            delay={4000}
            autohide
            bg="danger"
          >
            <Toast.Header>
              <strong className="me-auto">Ошибка загрузки</strong>
            </Toast.Header>
            <Toast.Body className="text-white">{toastMessage}</Toast.Body>
          </Toast>
        </ToastContainer>

        {!previewUrl && (
          <div className={styles.heroSection}>
            <h1 className="mb-4">Узнайте детали о продукте</h1>
            <p className="text-muted mb-5">
              Сфотографируйте этикетку состава для поиска E-добавок и
              аллергенов, или воспользуйтесь сканером штрихкодов для быстрого
              поиска продукта в базе.
            </p>

            <input
              type="file"
              accept="image/jpeg, image/png, image/heic"
              className={styles.fileInput}
              ref={fileInputRef}
              onChange={handleFileChange}
            />

            <Row className="g-4 justify-content-center">
              <Col md={6}>
                <div
                  className={`${styles.uploadBox} ${isDragging ? styles.dragActive : ""}`}
                  onClick={handleBoxClick}
                  onDragEnter={handleDragEnter}
                  onDragLeave={handleDragLeave}
                  onDragOver={handleDragOver}
                  onDrop={handleDrop}
                >
                  <div className={styles.uploadIcon}>📷</div>
                  <h4>Анализ состава</h4>
                  <p className="text-muted mt-2">
                    Загрузите или перетащите фото (JPG, PNG, HEIC)
                  </p>
                  <p className="text-muted" style={{ fontSize: "0.8rem" }}>
                    до {MAX_FILE_SIZE_MB} МБ
                  </p>
                </div>
              </Col>
              <Col md={6}>
                <div
                  className={styles.uploadBox}
                  onClick={handleBarcodeBoxClick}
                >
                  <div className={styles.uploadIcon}>📊</div>
                  <h4>Штрихкод продукта</h4>
                  <p className="text-muted mt-2">
                    Поиск продукта в базе данных
                  </p>
                </div>
              </Col>
            </Row>
          </div>
        )}

        {previewUrl && (
          <div className="mt-4">
            <Row className="g-4 mb-4">
              <Col
                xs={12}
                lg={scanResult ? 6 : 12}
                className={!scanResult ? "text-center" : ""}
              >
                <img
                  src={previewUrl}
                  alt="Scanned label"
                  className={styles.previewImage}
                  style={!scanResult ? { maxWidth: "500px" } : {}}
                />
              </Col>

              {scanResult && (
                <Col xs={12} lg={6}>
                  <div className={styles.parsedTextContainer}>
                    <div
                      className="text-white mb-2"
                      style={{ fontSize: "0.8rem", opacity: 0.7 }}
                    >
                      &gt; ПЕРВИЧНЫЙ РЕЗУЛЬТАТ РАСПОЗНАВАНИЯ ТЕКСТА
                    </div>
                    <pre>{scanResult.parsedText}</pre>
                  </div>
                </Col>
              )}
            </Row>

            {loading && (
              <div className="text-center my-5">
                <Spinner
                  animation="border"
                  variant="success"
                  className="mb-3"
                />
                <h5>Анализируем...</h5>
              </div>
            )}

            {scanResult && !loading && (
              <div className="mt-5">
                <AdditivesBlock
                  additives={detailedAdditives}
                  title="Найденные добавки"
                  emptyText="Мы не нашли ни одной известной пищевой добавки (Е-кода) на этом фото."
                  headerAction={
                    <Button variant="outline-success" onClick={resetScanner}>
                      Сбросить результат
                    </Button>
                  }
                />

                <AllergensBlock allergens={scanResult.allergens} />

                <ControversialBlock controversial={scanResult.controversial} />
              </div>
            )}
          </div>
        )}

        {error && !loading && (
          <Alert variant="danger" className="mt-4 text-center">
            {error}
            <div className="mt-3">
              <Button variant="outline-danger" onClick={resetScanner}>
                Попробовать снова
              </Button>
            </div>
          </Alert>
        )}

        {/* МОДАЛКИ */}
        <Modal
          show={showBarcodeModal}
          onHide={() => setShowBarcodeModal(false)}
          centered
        >
          <Modal.Header closeButton>
            <Modal.Title>Сканирование штрихкода</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            {barcodeError && (
              <Alert
                variant="danger"
                className="d-flex flex-column align-items-center"
              >
                <span className="mb-2 text-center">{barcodeError}</span>
                <Button
                  variant="outline-danger"
                  size="sm"
                  onClick={() => {
                    setBarcodeError(null);
                    setScannerKey((prev) => prev + 1);
                  }}
                >
                  Попробовать снова
                </Button>
              </Alert>
            )}
            <div id="barcode-reader" style={{ width: "100%" }}></div>

            {!showManualInput ? (
              <p className="text-center mt-3 text-muted">
                Наведите камеру устройства на штрихкод продукта
              </p>
            ) : (
              <div
                className="mt-3 p-4 rounded shadow-sm"
                style={{
                  backgroundColor: "#fff3cd",
                  border: "1px solid #ffe69c",
                }}
              >
                <p
                  className="text-center mb-3"
                  style={{
                    color: "#856404",
                    fontWeight: "500",
                    fontSize: "1.05rem",
                  }}
                >
                  Не удается отсканировать? Введите штрихкод вручную:
                </p>
                <div className="d-flex gap-2 justify-content-center">
                  <Form.Control
                    type="text"
                    placeholder="Например: 4601234567890"
                    value={manualBarcode}
                    onChange={(e) => setManualBarcode(e.target.value)}
                    style={{ maxWidth: "300px" }}
                    disabled={loading}
                  />
                  <Button
                    variant="warning"
                    style={{ color: "#856404", fontWeight: "600" }}
                    disabled={!manualBarcode.trim() || loading}
                    onClick={() => {
                      handleBarcodeScanned(manualBarcode);
                    }}
                  >
                    {loading ? "Поиск..." : "Найти"}
                  </Button>
                </div>
              </div>
            )}
          </Modal.Body>
        </Modal>

        <AuthModal
          show={showAuthModal}
          onHide={() => setShowAuthModal(false)}
          message="Функция сканирования доступна только авторизованным пользователям. Пожалуйста, войдите в свой аккаунт или зарегистрируйтесь."
        />

        <Modal
          show={showLimitModal}
          onHide={() => setShowLimitModal(false)}
          centered
          backdrop="static"
        >
          <Modal.Body className="text-center p-5">
            <div style={{ fontSize: "4.5rem", lineHeight: 1 }}>💎</div>
            <h3 className="mt-4 fw-bold">Лимит исчерпан</h3>
            <p className="text-muted mt-3 mb-4" style={{ fontSize: "1.05rem" }}>
              Вы использовали все доступные бесплатные сканирования. Оформите
              Premium-подписку, чтобы получить <b>безлимитный доступ</b> ко всем
              функциям проекта!
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
              Позже
            </Button>
          </Modal.Body>
        </Modal>
      </Container>

      {!previewUrl && <MethodologyBlock />}
    </>
  );
};

export default HomePage;
