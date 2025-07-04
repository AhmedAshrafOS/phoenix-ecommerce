package com.vodafone.ecommerce.security.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class CookieService {

    private CookieService() {
    }

    public static void addHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        clearCookie(response, name);
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
        log.debug("Added HttpOnly cookie: {} with maxAge {}s", name, maxAgeSeconds);
    }

    public static void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.debug("Cleared cookie: {}", name);
    }

    public static String getCookieValue(Cookie[] cookies, String name) {
        if (cookies == null) {
            log.warn("No cookies found when trying to get {}", name);
            return null;
        }

        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Cookie not found: {}", name);
                    return null;
                });
    }
}
