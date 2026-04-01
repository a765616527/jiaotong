package com.traffic.util;

import com.traffic.common.ApiException;
import com.traffic.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new ApiException(401, "未登录或登录状态无效");
        }
        return loginUser;
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static boolean hasRole(String roleCode) {
        return currentUser().getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(roleCode));
    }

    public static void requireAdmin() {
        if (!hasRole("ROLE_ADMIN")) {
            throw new ApiException(403, "仅管理员可操作");
        }
    }
}
