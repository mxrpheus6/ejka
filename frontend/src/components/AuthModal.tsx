import React from "react";
import { Modal, Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

interface AuthModalProps {
  show: boolean;
  onHide: () => void;
  message?: string;
}

const AuthModal: React.FC<AuthModalProps> = ({
  show,
  onHide,
  message = "Эта функция доступна только авторизованным пользователям. Пожалуйста, войдите в свой аккаунт.",
}) => {
  const navigate = useNavigate();

  const handleLoginClick = () => {
    onHide();
    navigate("/profile");
  };

  return (
    <Modal show={show} onHide={onHide} centered>
      <Modal.Header closeButton>
        <Modal.Title>Требуется авторизация</Modal.Title>
      </Modal.Header>
      <Modal.Body>{message}</Modal.Body>
      <Modal.Footer>
        <Button variant="outline-secondary" onClick={onHide}>
          Отмена
        </Button>
        <Button
          variant="success"
          style={{ backgroundColor: "#539155", borderColor: "#539155" }}
          onClick={handleLoginClick}
        >
          Авторизоваться
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default AuthModal;
