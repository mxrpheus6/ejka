import React, { useState } from "react";
import { Container, Form, Button, Alert, Spinner } from "react-bootstrap";
import { useDispatch } from "react-redux";
import { setAuthTokens } from "../store/authSlice";
import { authApi } from "../api/authApi";
import styles from "./AuthPage.module.css";

const AuthPage: React.FC = () => {
  const dispatch = useDispatch();

  const [isLoginMode, setIsLoginMode] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [formData, setFormData] = useState({
    email: "",
    password: "",
    username: "",
    name: "",
    birthDate: "",
  });

  const resetForm = () => {
    setFormData({
      email: "",
      password: "",
      username: "",
      name: "",
      birthDate: "",
    });
    setError(null);
    setFieldErrors({});
  };

  const getMaxBirthDate = () => {
    const today = new Date();
    const maxDate = new Date(
      today.getFullYear() - 14,
      today.getMonth(),
      today.getDate(),
    );
    return maxDate.toISOString().split("T")[0];
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (fieldErrors[e.target.name]) {
      setFieldErrors({ ...fieldErrors, [e.target.name]: "" });
    }
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const usernameRegex = /^[a-zA-Z0-9._]+$/;

    if (!formData.email.trim()) {
      errors.email = "Email не может быть пустым";
    } else if (!emailRegex.test(formData.email)) {
      errors.email = "Введите корректный email";
    }

    if (!formData.password) {
      errors.password = "Пароль не может быть пустым";
    } else if (formData.password.length < 8 || formData.password.length > 16) {
      errors.password = "Пароль должен содержать от 8 до 16 символов";
    }

    if (!isLoginMode) {
      if (!formData.username.trim()) {
        errors.username = "Имя пользователя не может быть пустым";
      } else if (formData.username.length > 32) {
        errors.username = "Имя пользователя не должно превышать 32 символа";
      } else if (!usernameRegex.test(formData.username)) {
        errors.username =
          "Разрешены только латинские буквы, цифры, точки и подчеркивания";
      }

      if (!formData.name.trim()) {
        errors.name = "Имя не может быть пустым";
      } else if (formData.name.length > 50) {
        errors.name = "Имя не должно превышать 50 символов";
      }

      if (!formData.birthDate) {
        errors.birthDate = "Дата рождения обязательна";
      } else {
        const birthDate = new Date(formData.birthDate);
        const today = new Date();
        const maxAllowedDate = new Date(
          today.getFullYear() - 14,
          today.getMonth(),
          today.getDate(),
        );

        if (birthDate >= today) {
          errors.birthDate = "Дата рождения должна быть в прошлом";
        } else if (birthDate > maxAllowedDate) {
          errors.birthDate =
            "Регистрация доступна только пользователям старше 14 лет";
        }
      }
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!validateForm()) return;

    setLoading(true);

    try {
      let res;
      if (isLoginMode) {
        res = await authApi.login({
          email: formData.email,
          password: formData.password,
        });
      } else {
        res = await authApi.register({
          email: formData.email,
          password: formData.password,
          username: formData.username,
          name: formData.name,
          birthDate: formData.birthDate,
        });
      }
      dispatch(setAuthTokens(res));
      resetForm();
    } catch (err: any) {
      if (err.response) {
        const status = err.response.status;
        const responseData = err.response.data;

        if (status === 401) {
          // Проверяем, есть ли конкретное сообщение от сервера
          if (responseData && responseData.message) {
            setError(responseData.message);
          } else {
            setError("Неверный email или пароль");
          }
          return;
        }

        if (responseData) {
          if (responseData.errors && typeof responseData.errors === "object") {
            setFieldErrors(responseData.errors);
          } else if (
            status === 400 &&
            typeof responseData === "object" &&
            !responseData.message
          ) {
            setFieldErrors(responseData);
          } else {
            setError(responseData.message || `Ошибка сервера: ${status}`);
          }
        } else {
          setError(`Произошла ошибка (код ${status})`);
        }
      } else {
        setError("Нет связи с сервером. Проверьте подключение.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="mt-4 pb-5">
      <div className={styles.authContainer}>
        <h3 className={styles.title}>{isLoginMode ? "Вход" : "Регистрация"}</h3>

        {error && <Alert variant="danger">{error}</Alert>}

        <Form onSubmit={handleSubmit} noValidate>
          {!isLoginMode && (
            <>
              <Form.Group className="mb-3">
                <Form.Label>Имя пользователя</Form.Label>
                <Form.Control
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  isInvalid={!!fieldErrors.username}
                />
                <Form.Control.Feedback type="invalid">
                  {fieldErrors.username}
                </Form.Control.Feedback>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Имя</Form.Label>
                <Form.Control
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  isInvalid={!!fieldErrors.name}
                />
                <Form.Control.Feedback type="invalid">
                  {fieldErrors.name}
                </Form.Control.Feedback>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Дата рождения</Form.Label>
                <Form.Control
                  type="date"
                  name="birthDate"
                  value={formData.birthDate}
                  onChange={handleChange}
                  max={getMaxBirthDate()}
                  isInvalid={!!fieldErrors.birthDate}
                />
                <Form.Control.Feedback type="invalid">
                  {fieldErrors.birthDate}
                </Form.Control.Feedback>
              </Form.Group>
            </>
          )}

          <Form.Group className="mb-3">
            <Form.Label>Email</Form.Label>
            <Form.Control
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              isInvalid={!!fieldErrors.email}
            />
            <Form.Control.Feedback type="invalid">
              {fieldErrors.email}
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-4">
            <Form.Label>Пароль</Form.Label>
            <Form.Control
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              isInvalid={!!fieldErrors.password}
            />
            <Form.Control.Feedback type="invalid">
              {fieldErrors.password}
            </Form.Control.Feedback>
          </Form.Group>

          <Button
            variant="success"
            type="submit"
            className="w-100"
            disabled={loading}
            style={{ backgroundColor: "#539155", borderColor: "#539155" }}
          >
            {loading ? (
              <Spinner size="sm" />
            ) : isLoginMode ? (
              "Войти"
            ) : (
              "Зарегистрироваться"
            )}
          </Button>
        </Form>

        <div className={styles.switchMode}>
          {isLoginMode ? "Нет аккаунта? " : "Уже есть аккаунт? "}
          <span
            onClick={() => {
              setIsLoginMode(!isLoginMode);
              resetForm();
            }}
          >
            {isLoginMode ? "Создать" : "Войти"}
          </span>
        </div>
      </div>
    </Container>
  );
};

export default AuthPage;
