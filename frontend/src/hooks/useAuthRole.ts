import { useSelector } from "react-redux";
import type { RootState } from "../store/store";

export const useAuthRole = () => {
  const user = useSelector((state: RootState) => state.auth.user);

  const role = user?.role || "ROLE_USER";

  const isModerator = role === "ROLE_MODERATOR";

  return { user, role, isModerator };
};
