package com.rota.security;

import com.rota.entity.Rol;
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

    /**
     * HU-05: indica si el usuario autenticado es operativo (ROLE_REPOSITOR) y no debe ver datos financieros.
     */
    public static boolean esRepositor() {
        Authentication auth = obtenerAutenticacion();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> Rol.ROLE_REPOSITOR.name().equals(a.getAuthority()));
    }

    private static Authentication obtenerAutenticacion() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }
}
