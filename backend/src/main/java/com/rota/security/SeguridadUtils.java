package com.rota.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilidades para consultar el usuario autenticado en el SecurityContext.
 */
public final class SeguridadUtils {

    private SeguridadUtils() {
    }

    /**
     * Devuelve el email (username) del usuario autenticado, o null si no hay sesión.
     */
    public static String obtenerEmailAutenticado() {
        Authentication auth = obtenerAutenticacion();
        return auth != null ? auth.getName() : null;
    }

    private static Authentication obtenerAutenticacion() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }
}
