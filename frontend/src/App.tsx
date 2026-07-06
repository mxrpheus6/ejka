import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { Provider, useDispatch, useSelector } from "react-redux";
import { Spinner, Container } from "react-bootstrap";
import { store } from "./store/store";
import type { RootState } from "./store/store";
import { setUser, clearAuth } from "./store/authSlice";

import { authApi } from "./api/authApi";

import Header from "./components/Header";
import AdditivesPage from "./pages/AdditivesPage";
import AdditiveDetailsPage from "./pages/AdditiveDetailsPage";
import ProfilePage from "./pages/ProfilePage";
import AuthPage from "./pages/AuthPage";
import "./index.css";
import HomePage from "./pages/HomePage";
import SubscriptionPage from "./pages/SubscriptionPage";
import ProductsPage from "./pages/ProductsPage";
import ProductDetailsPage from "./pages/ProductsDetailsPage";
import AddProductPage from "./pages/AddProductPage";
import EditProductPage from "./pages/EditProductPage";
import AddAdditivePage from "./pages/AddAdditivePage";
import EditAdditivePage from "./pages/EditAdditivePage";

const AppContent: React.FC = () => {
  const dispatch = useDispatch();
  const { isAuthenticated, user } = useSelector(
    (state: RootState) => state.auth,
  );

  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      if (isAuthenticated && !user) {
        try {
          const userData = await authApi.getUserProfile();
          dispatch(setUser(userData));
        } catch (error) {
          console.error("Не удалось восстановить сессию", error);
          dispatch(clearAuth());
        }
      }
      setIsInitializing(false);
    };

    initAuth();
  }, [isAuthenticated, user, dispatch]);

  if (isInitializing) {
    return (
      <Container
        className="d-flex justify-content-center align-items-center"
        style={{ minHeight: "100vh" }}
      >
        <Spinner animation="border" variant="success" />
      </Container>
    );
  }

  return (
    <Router>
      <Header />
      <main className="main-content">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/products/me" element={<ProductsPage />} />
          <Route path="/products/:id" element={<ProductDetailsPage />} />
          <Route path="/products/create" element={<AddProductPage />} />
          <Route path="/products/:id/edit" element={<EditProductPage />} />
          <Route path="/additives" element={<AdditivesPage />} />
          <Route path="/additives/:id" element={<AdditiveDetailsPage />} />
          <Route path="/additives/create" element={<AddAdditivePage />} />
          <Route path="/additives/:id/edit" element={<EditAdditivePage />} />
          <Route
            path="/profile"
            element={isAuthenticated ? <ProfilePage /> : <AuthPage />}
          />
          <Route path="/subscription" element={<SubscriptionPage />} />
        </Routes>
      </main>
    </Router>
  );
};

const App: React.FC = () => {
  return (
    <Provider store={store}>
      <AppContent />
    </Provider>
  );
};

export default App;
