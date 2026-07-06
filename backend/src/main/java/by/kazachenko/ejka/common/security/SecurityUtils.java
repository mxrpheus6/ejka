package by.kazachenko.ejka.common.security;

import by.kazachenko.ejka.user.model.enums.Role;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    
    public UUID getLoggedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return UUID.fromString(principal.getId());
    }

    public Role getLoggedUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return principal.getRole();
    }

}
