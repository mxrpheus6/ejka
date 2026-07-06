import React, { useState, useRef, useEffect } from "react";
import {
  Container,
  Button,
  Spinner,
  Modal,
  Badge,
  ProgressBar,
} from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import type { RootState } from "../store/store";
import { clearAuth, setUser } from "../store/authSlice";
import { authApi } from "../api/authApi";
import { useNavigate } from "react-router-dom";
import { useAuthRole } from "../hooks/useAuthRole";
import styles from "./ProfilePage.module.css";

const MINIO_AVATARS_BASE_URL = import.meta.env.VITE_MINIO_AVATARS_URL || "";

const DefaultAvatar = () => (
  <svg viewBox="0 0 24 24" fill="currentColor" width="100%" height="100%">
    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z" />
  </svg>
);

const ProfilePage: React.FC = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state: RootState) => state.auth);

  const { role, isModerator } = useAuthRole();

  const [loading, setLoading] = useState(false);
  const [isAvatarUploading, setIsAvatarUploading] = useState(false);
  const [showAvatarModal, setShowAvatarModal] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 👇 Оставили только ОДИН правильный метод загрузки профиля
  const loadProfile = async () => {
    try {
      const profile = await authApi.getUserProfile();
      dispatch(setUser(profile));
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const handleLogout = async () => {
    setLoading(true);
    try {
      await authApi.logout();
    } catch (err) {
      console.error(err);
    } finally {
      dispatch(clearAuth());
      setLoading(false);
    }
  };

  const handleDeleteProfile = async () => {
    if (
      !window.confirm(
        "Вы уверены, что хотите удалить профиль? Это действие необратимо.",
      )
    )
      return;
    setLoading(true);
    try {
      await authApi.deleteUserProfile();
      dispatch(clearAuth());
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAvatarClick = () => {
    if (user?.avatarKey) {
      setShowAvatarModal(true);
    } else {
      fileInputRef.current?.click();
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      alert("Файл слишком большой. Максимальный размер 5МБ.");
      return;
    }

    setIsAvatarUploading(true);
    setShowAvatarModal(false);

    try {
      await authApi.uploadAvatar(file);
      await loadProfile();
    } catch (err) {
      console.error("Ошибка при загрузке аватара:", err);
      alert("Не удалось загрузить аватар. Попробуйте позже.");
    } finally {
      setIsAvatarUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const handleDeleteAvatar = async () => {
    if (!window.confirm("Удалить фото профиля?")) return;

    setIsAvatarUploading(true);
    setShowAvatarModal(false);

    try {
      await authApi.deleteAvatar();
      await loadProfile();
    } catch (err) {
      console.error("Ошибка при удалении аватара:", err);
      alert("Не удалось удалить аватар.");
    } finally {
      setIsAvatarUploading(false);
    }
  };

  // 👇 ХУК ДЛЯ АНИМАЦИИ ПРОГРЕСС-БАРА
  const [animatedPercentage, setAnimatedPercentage] = useState(100); // Начинаем со 100%, чтобы полоска "убывала"

  useEffect(() => {
    if (user) {
      const remainingScans = Math.max(0, 5 - (user.scansCount || 0));
      const targetPercentage = (remainingScans / 5) * 100;

      // Ждем 50мс, чтобы React успел отрендерить 100%, а затем меняем на реальное число
      const timer = setTimeout(() => {
        setAnimatedPercentage(targetPercentage);
      }, 50);

      return () => clearTimeout(timer);
    }
  }, [user?.scansCount]);

  if (!user) return null;

  const formattedBirthDate = new Date(user.birthDate).toLocaleDateString(
    "ru-RU",
  );
  const formattedRegDate = new Date(user.registrationDate).toLocaleDateString(
    "ru-RU",
  );
  const formattedPremiumDate = user.premiumUntil
    ? new Date(user.premiumUntil).toLocaleDateString("ru-RU")
    : "—";

  const remainingScans = Math.max(0, 5 - (user.scansCount || 0));

  return (
    <Container className="mt-4 pb-5">
      <div className={styles.profileCard}>
        <div className={styles.profileHeader}>
          <div className={styles.avatarWrapper} onClick={handleAvatarClick}>
            {isAvatarUploading ? (
              <div className={styles.avatarLoadingOverlay}>
                <Spinner animation="border" variant="light" size="sm" />
              </div>
            ) : user.avatarKey ? (
              <img
                src={`${MINIO_AVATARS_BASE_URL}/${user.avatarKey}`}
                alt="Аватар пользователя"
                className={styles.avatarImage}
              />
            ) : (
              <div className={styles.defaultAvatar}>
                <DefaultAvatar />
              </div>
            )}

            {!isAvatarUploading && (
              <div className={styles.avatarOverlay}>
                <span>{user.avatarKey ? "Просмотр" : "Добавить"}</span>
              </div>
            )}
          </div>

          <input
            type="file"
            accept="image/png, image/jpeg, image/webp"
            ref={fileInputRef}
            onChange={handleFileChange}
            style={{ display: "none" }}
          />

          <div className={styles.headerInfo}>
            <div className="d-flex align-items-center gap-2 mb-1 flex-wrap">
              <h2 className="mb-0">{user.name}</h2>
              {isModerator && (
                <Badge
                  bg="primary"
                  className="fw-semibold shadow-sm"
                  style={{ fontSize: "0.8rem" }}
                >
                  Модератор
                </Badge>
              )}
              {role === "ROLE_ADMIN" && (
                <Badge
                  bg="danger"
                  className="fw-semibold shadow-sm"
                  style={{ fontSize: "0.8rem" }}
                >
                  Администратор
                </Badge>
              )}
            </div>
            <p className="text-muted mb-0">@{user.username}</p>
          </div>
        </div>

        <hr className="my-4" />

        <div
          className={`d-flex justify-content-between align-items-center flex-wrap gap-3 p-4 rounded-4 mb-4 ${
            user.isPremium
              ? user.cancelAtPeriodEnd
                ? "bg-warning-subtle"
                : "bg-success-subtle"
              : "bg-light border"
          }`}
        >
          <div>
            <div className="text-muted small mb-1">Статус подписки</div>
            {user.isPremium ? (
              <>
                <div className="fw-bold fs-5 mb-1 d-flex align-items-center gap-2">
                  {user.cancelAtPeriodEnd
                    ? "Автопродление отключено ⏳"
                    : "Premium ⭐"}
                </div>
                <div className="small" style={{ color: "rgba(0,0,0,0.7)" }}>
                  {user.cancelAtPeriodEnd
                    ? "Доступ активен до: "
                    : "Следующее списание: "}
                  <strong>{formattedPremiumDate}</strong>
                </div>
              </>
            ) : (
              <>
                <div className="fw-bold fs-5 mb-2">
                  {isModerator ? (
                    <span className="text-primary d-flex align-items-center gap-2">
                      Безлимит{" "}
                      <span style={{ fontSize: "1.5rem", lineHeight: 1 }}>
                        ∞
                      </span>
                    </span>
                  ) : (
                    `Осталось: ${remainingScans} из 5`
                  )}
                </div>

                {!isModerator && (
                  <ProgressBar
                    variant="success"
                    now={animatedPercentage} // 👈 Подставили анимированный стейт
                    style={{
                      height: "8px",
                      width: "200px",
                      backgroundColor: "#e9ecef",
                      // Переход сработает за счет родных стилей Bootstrap .progress-bar
                    }}
                  />
                )}

                <div className="small text-muted mt-2">
                  {isModerator
                    ? "Спасибо за развитие базы продуктов!"
                    : "Обновляется каждый день"}
                </div>
              </>
            )}
          </div>

          <div>
            {user.isPremium ? (
              <Button
                variant={
                  user.cancelAtPeriodEnd ? "outline-dark" : "outline-success"
                }
                className={`fw-semibold shadow-sm ${styles.whiteBgButton}`}
                onClick={() => navigate("/subscription")}
              >
                Управление подпиской
              </Button>
            ) : isModerator ? (
              <div
                className="d-flex align-items-center gap-2 px-3 py-2 rounded-pill"
                style={{
                  backgroundColor: "rgba(83, 145, 85, 0.1)",
                  color: "#539155",
                  fontWeight: "600",
                }}
              >
                ✓ Premium функции активны
              </div>
            ) : (
              <Button
                variant="success"
                className="fw-semibold rounded-pill px-4 py-2 shadow-sm"
                style={{ backgroundColor: "#539155", border: "none" }}
                onClick={() => navigate("/subscription")}
              >
                ✨ Перейти на Premium
              </Button>
            )}
          </div>
        </div>

        <div className={styles.profileGrid}>
          <div className={styles.profileItem}>
            <div className={styles.profileLabel}>Email</div>
            <div className={styles.profileValue}>{user.email}</div>
          </div>
          <div className={styles.profileItem}>
            <div className={styles.profileLabel}>Дата рождения</div>
            <div className={styles.profileValue}>{formattedBirthDate}</div>
          </div>
          <div className={styles.profileItem}>
            <div className={styles.profileLabel}>Дата регистрации</div>
            <div className={styles.profileValue}>{formattedRegDate}</div>
          </div>
        </div>

        <div className="d-flex gap-3 mt-4 pt-3 border-top">
          <Button
            variant="outline-secondary"
            onClick={handleLogout}
            disabled={loading}
          >
            Выйти
          </Button>
          <Button
            variant="outline-danger"
            onClick={handleDeleteProfile}
            disabled={loading}
          >
            Удалить аккаунт
          </Button>
        </div>
      </div>

      <Modal
        show={showAvatarModal}
        onHide={() => setShowAvatarModal(false)}
        centered
      >
        <Modal.Header closeButton className="border-0 pb-0"></Modal.Header>
        <Modal.Body className="text-center pt-0">
          <img
            src={`${MINIO_AVATARS_BASE_URL}/${user.avatarKey}`}
            alt="Крупный аватар"
            style={{
              maxWidth: "100%",
              maxHeight: "400px",
              borderRadius: "12px",
              objectFit: "contain",
            }}
          />
        </Modal.Body>
        <Modal.Footer className="justify-content-center border-0 pt-0">
          <Button
            variant="outline-primary"
            onClick={() => fileInputRef.current?.click()}
          >
            Изменить
          </Button>
          <Button variant="outline-danger" onClick={handleDeleteAvatar}>
            Удалить
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default ProfilePage;
