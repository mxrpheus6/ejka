import React, { useEffect, useState } from "react";
import {
  Container,
  Card,
  Button,
  ListGroup,
  Alert,
  Spinner,
} from "react-bootstrap";
import { useSearchParams, useNavigate } from "react-router-dom"; // 👈 Добавили useNavigate
import { useSelector, useDispatch } from "react-redux";
import { paymentApi } from "../api/paymentApi";
import { authApi } from "../api/authApi";
import { setUser } from "../store/authSlice";
import { useAuthRole } from "../hooks/useAuthRole"; // 👈 Импортируем наш хук
import type { PlanResponse } from "../types/payments";
import BynIcon from "../assets/BYN_symbol.svg";

const SubscriptionPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate(); // 👈 Инициализируем навигацию
  const { user, isAuthenticated } = useSelector((state: any) => state.auth);
  const dispatch = useDispatch();

  const { isModerator } = useAuthRole(); // 👈 Достаем роль

  const [plan, setPlan] = useState<PlanResponse | null>(null);

  const isCancelled = searchParams.get("error") === "payment_cancelled";
  const isSuccess = !!searchParams.get("session_id");

  useEffect(() => {
    // 👈 Грузим тариф ТОЛЬКО если юзер не премиум и НЕ модератор
    if (!user?.isPremium && !isModerator) {
      paymentApi
        .getPlanDetails()
        .then((data) => setPlan(data))
        .catch((err) => console.error("Ошибка при загрузке тарифа", err));
    }
  }, [user?.isPremium, isModerator]);

  useEffect(() => {
    if (isSuccess && isAuthenticated) {
      authApi
        .getUserProfile()
        .then((updatedUser) => dispatch(setUser(updatedUser)));
    }
  }, [isSuccess, isAuthenticated, dispatch]);

  const handleSubscribe = async () => {
    try {
      const url = await paymentApi.createCheckoutSession();
      window.location.href = url;
    } catch (err) {
      alert("Ошибка при создании сессии");
    }
  };

  const handleCancelSubscription = async () => {
    if (
      window.confirm(
        "Вы уверены? Подписка будет активна до конца оплаченного периода, но не продлится.",
      )
    ) {
      try {
        await paymentApi.cancelSubscription();
        alert("Автопродление отключено.");
        const updatedUser = await authApi.getUserProfile();
        dispatch(setUser(updatedUser));
      } catch (err) {
        alert("Ошибка при отмене");
      }
    }
  };

  return (
    <Container className="mt-5 d-flex flex-column align-items-center pb-5">
      {isCancelled && (
        <Alert variant="warning" className="w-100 mb-4 text-center">
          Оплата была отменена. Попробуйте еще раз, если передумаете.
        </Alert>
      )}
      {isSuccess && (
        <Alert variant="success" className="w-100 mb-4 text-center">
          Спасибо! Подписка успешно активирована.
        </Alert>
      )}

      <Card
        style={{
          width: "26rem",
          borderRadius: "20px",
          border: "none",
          boxShadow: "0 15px 35px rgba(0,0,0,0.1)",
        }}
      >
        <Card.Body className="p-5 text-center">
          {user?.isPremium ? (
            // --- UI КАРТОЧКА ДЛЯ ТЕХ, У КОГО УЖЕ ЕСТЬ ПОДПИСКА ---
            <>
              <div style={{ fontSize: "3.5rem" }}>
                {user.cancelAtPeriodEnd ? "⏳" : "⭐"}
              </div>
              <Card.Title
                className="mt-3"
                style={{ fontWeight: "800", fontSize: "1.6rem" }}
              >
                {user.cancelAtPeriodEnd
                  ? "Автопродление отключено"
                  : "Ваш статус: Premium"}
              </Card.Title>
              <p className="text-muted mb-4">
                {user.cancelAtPeriodEnd
                  ? "Доступ ко всем функциям сохранится до конца оплаченного периода."
                  : "Спасибо, что поддерживаете проект!"}
              </p>

              <div
                className={`p-3 rounded-4 mb-4 text-start ${
                  user.cancelAtPeriodEnd ? "bg-warning" : "bg-light"
                }`}
                style={
                  user.cancelAtPeriodEnd
                    ? ({ "--bs-bg-opacity": 0.15 } as React.CSSProperties)
                    : {}
                }
              >
                <div className="small text-muted mb-1">
                  {user.cancelAtPeriodEnd
                    ? "Доступ активен до:"
                    : "Следующее списание:"}
                </div>
                <div style={{ fontWeight: "600", fontSize: "1.1rem" }}>
                  {user.premiumUntil
                    ? new Date(user.premiumUntil).toLocaleDateString()
                    : "—"}
                </div>
              </div>

              {!user.cancelAtPeriodEnd && (
                <Button
                  variant="outline-danger"
                  className="w-100 rounded-pill py-2"
                  onClick={handleCancelSubscription}
                >
                  Отменить подписку
                </Button>
              )}
            </>
          ) : isModerator ? (
            // --- 👈 ОБНОВЛЕННЫЙ UI ДЛЯ МОДЕРАТОРОВ (В СТИЛЕ PREMIUM) ---
            <>
              <div style={{ fontSize: "3.5rem" }}>⭐</div>
              <Card.Title
                className="mt-3"
                style={{ fontWeight: "800", fontSize: "1.6rem" }}
              >
                Ваш статус: Premium
              </Card.Title>
              <p className="text-muted mb-4 mt-3">
                Все платные функции разблокированы в знак благодарности за ваш
                вклад в чистоту базы данных Ежки.
              </p>

              <div className="p-3 rounded-4 mb-4 text-start bg-light">
                <div className="small text-muted mb-1">Срок действия:</div>
                <div style={{ fontWeight: "600", fontSize: "1.1rem" }}>
                  Бессрочно (Привилегия модератора)
                </div>
              </div>

              <ListGroup variant="flush" className="text-start mb-4">
                <ListGroup.Item className="border-0 px-0">
                  ✅ Безлимитные сканирования состава
                </ListGroup.Item>
                <ListGroup.Item className="border-0 px-0">
                  ✅ Прямое добавление продуктов
                </ListGroup.Item>
              </ListGroup>

              <Button
                variant="success"
                size="lg"
                className="w-100 rounded-pill py-3"
                style={{
                  backgroundColor: "#539155",
                  border: "none",
                  fontWeight: "600",
                }}
                onClick={() => navigate("/")}
              >
                В каталог продуктов
              </Button>
            </>
          ) : !plan ? (
            // --- UI СОСТОЯНИЕ ЗАГРУЗКИ ТАРИФА ---
            <div
              className="d-flex flex-column justify-content-center align-items-center"
              style={{ minHeight: "350px" }}
            >
              <Spinner
                animation="border"
                variant="success"
                style={{
                  width: "3.5rem",
                  height: "3.5rem",
                  borderWidth: "0.25em",
                }}
              />
              <div className="mt-4 text-muted fw-semibold">
                Подготавливаем тариф...
              </div>
            </div>
          ) : (
            // --- UI КАРТОЧКА ПОКУПКИ (ДАННЫЕ ЗАГРУЖЕНЫ) ---
            <>
              <div style={{ fontSize: "3.5rem" }}>💎</div>
              <Card.Title
                className="mt-3"
                style={{ fontWeight: "800", fontSize: "1.6rem" }}
              >
                {plan.name}
              </Card.Title>
              <div className="my-4 d-flex align-items-baseline justify-content-center">
                {/* Сумма */}
                <span
                  style={{
                    fontSize: "2.8rem",
                    fontWeight: "900",
                    lineHeight: "1",
                  }}
                >
                  {plan.price}
                </span>

                {/* Иконка BYN вместо текста */}
                <img
                  src={BynIcon}
                  alt="BYN"
                  style={{
                    height: "2.1rem", // Подбираем высоту под шрифт
                    marginLeft: "8px",
                    alignSelf: "center",
                    display: "block",
                    transform: "translateY(2px)",
                  }}
                />

                <span className="text-muted ms-1">
                  / {plan.interval === "month" ? "месяц" : plan.interval}
                </span>
              </div>
              <ListGroup variant="flush" className="text-start mb-4">
                <ListGroup.Item className="border-0 px-0">
                  ✅ Безлимитные сканирования состава
                </ListGroup.Item>
                <ListGroup.Item className="border-0 px-0">
                  ✅ Поддержка и развитие проекта
                </ListGroup.Item>
              </ListGroup>
              <Button
                variant="success"
                size="lg"
                className="w-100 rounded-pill py-3"
                style={{
                  backgroundColor: "#539155",
                  border: "none",
                  fontWeight: "600",
                }}
                onClick={handleSubscribe}
              >
                Подписаться
              </Button>
            </>
          )}

          {/* ДИСКЛЕЙМЕР (Прячем для модераторов без платной подписки) */}
          {(!isModerator || user?.isPremium) && (
            <div className="mt-4 pt-3 border-top">
              <p
                style={{
                  fontSize: "0.75rem",
                  color: "#adb5bd",
                  lineHeight: "1.2",
                }}
                className="text-start m-0"
              >
                {user?.cancelAtPeriodEnd
                  ? "Вы сможете оформить новую подписку после завершения срока действия текущего периода."
                  : "Подписка продлевается автоматически ежемесячно. Вы можете отменить её в любое время в настройках профиля. Списания происходят в 00:00 по часовому поясу UTC+03:00."}
              </p>
            </div>
          )}
        </Card.Body>
      </Card>
    </Container>
  );
};

export default SubscriptionPage;
