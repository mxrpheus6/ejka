import { configureStore, combineReducers } from "@reduxjs/toolkit";
import type { Action } from "@reduxjs/toolkit";
import additivesReducer from "./additivesSlice";
import authReducer from "./authSlice";
import scanReducer from "./scanSlice";
import productReducer from "./productsSlice";

const appReducer = combineReducers({
  additives: additivesReducer,
  auth: authReducer,
  scan: scanReducer,
  products: productReducer,
});

const rootReducer = (
  state: ReturnType<typeof appReducer> | undefined,
  action: Action,
) => {
  if (action.type === "auth/clearAuth") {
    state = undefined;
  }

  return appReducer(state, action);
};

export const store = configureStore({
  reducer: rootReducer,
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
