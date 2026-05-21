package com.example.smartconsult.auth;

import com.example.smartconsult.exception.BusinessException;

public final class CurrentUserContext {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(CurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    public static CurrentUser getRequired() {
        CurrentUser currentUser = CURRENT_USER.get();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }
        return currentUser;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
