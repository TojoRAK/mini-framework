package framework.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import framework.annotation.RequireAuth;
import framework.annotation.RequireRole;

public class AuthorizationChecker {

    private final String authSessionAttribute;
    private final String roleSessionAttribute;

    public AuthorizationChecker(String authSessionAttribute, String roleSessionAttribute) {
        this.authSessionAttribute = authSessionAttribute;
        this.roleSessionAttribute = roleSessionAttribute;
    }

    private boolean isAuthenticated(HttpServletRequest req) {
        if (authSessionAttribute == null || authSessionAttribute.isBlank())
            return false;
        HttpSession httpSession = req.getSession(false);
        return httpSession != null && httpSession.getAttribute(authSessionAttribute) != null;
    }

    private String getRole(HttpServletRequest req) {
        if (!isAuthenticated(req) || roleSessionAttribute == null || roleSessionAttribute.isBlank()) {
            return null;
        }
        return (String) req.getSession(false).getAttribute(roleSessionAttribute);
    }

    public boolean checkAuthentication(Method controllerMethod,
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        boolean requiresAuth = controllerMethod.isAnnotationPresent(RequireAuth.class)
                || controllerMethod.isAnnotationPresent(RequireRole.class);

        if (!requiresAuth || isAuthenticated(req)) {
            return true;
        }

        resp.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentification requise");

        return false;
    }

    public boolean checkRole(Method controllerMethod, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        if (!controllerMethod.isAnnotationPresent(RequireRole.class))
            return true;
        RequireRole annotation = controllerMethod.getAnnotation(RequireRole.class);
        List<String> authorizedRoles = Arrays.asList(annotation.values());
        String role = getRole(req);
        if (role == null || !authorizedRoles.contains(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Vous n'avez pas accès à ceci");
            return false;
        }
        return true;
    }

    public boolean checkAccess(Method controllerMethod,
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        if (!checkAuthentication(controllerMethod, req, resp)) {
            return false;
        }

        if (!checkRole(controllerMethod, req, resp)) {
            return false;
        }

        return true;
    }
}
