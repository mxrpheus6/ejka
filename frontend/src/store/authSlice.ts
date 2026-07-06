import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";
import type { UserResponse } from "../types/auth";

interface AuthState {
  isAuthenticated: boolean;
  user: UserResponse | null;
}

const initialState: AuthState = {
  isAuthenticated: !!localStorage.getItem("accessToken"),
  user: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setAuthTokens: (
      state,
      action: PayloadAction<{ accessToken: string; refreshToken: string }>,
    ) => {
      localStorage.setItem("accessToken", action.payload.accessToken);
      localStorage.setItem("refreshToken", action.payload.refreshToken);
      state.isAuthenticated = true;
    },
    clearAuth: (state) => {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      state.isAuthenticated = false;
      state.user = null;
    },
    setUser: (state, action: PayloadAction<UserResponse>) => {
      state.user = action.payload;
    },
  },
});

export const { setAuthTokens, clearAuth, setUser } = authSlice.actions;
export default authSlice.reducer;
