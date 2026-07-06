import React from "react";
import { Navbar, Nav, Container } from "react-bootstrap";
import { Link } from "react-router-dom";
import styles from "./Header.module.css";

const Header: React.FC = () => {
  return (
    <Navbar expand="lg" sticky="top" className={styles.customHeader}>
      <Container>
        <Navbar.Brand>Ежка</Navbar.Brand>

        <Navbar.Toggle aria-controls="basic-navbar-nav" />

        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/">
              Главное меню
            </Nav.Link>
            <Nav.Link as={Link} to="/products">
              Продукты
            </Nav.Link>
            <Nav.Link as={Link} to="/additives">
              Добавки
            </Nav.Link>
            <Nav.Link as={Link} to="/subscription">
              Подписка
            </Nav.Link>
          </Nav>

          <Nav>
            <Nav.Link as={Link} to="/profile">
              Профиль
            </Nav.Link>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header;
