package framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import framework.annotation.Json;
import framework.annotation.RequireAuth;
import framework.utils.FileMap;
import framework.utils.FileUtils;
import framework.utils.ModelAndView;
import framework.utils.SessionFw;
import framework.utils.SessionUtils;
import framework.utils.UrlMethod;
// import tools.jackson.databind.ObjectMapper;

@MultipartConfig
public class FrontController extends HttpServlet {

    private Map<UrlMethod, Method> urlControllers = new HashMap<>();
    private String prefix;
    private String suffix;
    private String uploadPath;
    private ApplicationContext springContext;
    private Map<String, SessionFw> sessions;
    private String authSessionAttribute;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        // listController = (List<String>)
        // getServletContext().getAttribute("listController");
        System.out.println(
                "FrontController : " +
                        getServletContext().getAttribute("urlControllers"));
        urlControllers = (Map<UrlMethod, Method>) getServletContext().getAttribute("urlControllers");
        prefix = (String) getServletContext().getAttribute("prefix");
        suffix = (String) getServletContext().getAttribute("suffix");
        uploadPath = (String) getServletContext().getAttribute("uploadLocation");
        springContext = (ApplicationContext) getServletContext().getAttribute("springContext");
        sessions = (Map<String, SessionFw>) getServletContext().getAttribute("sessions");
        authSessionAttribute = (String) getServletContext().getAttribute("authSessionAttribute");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processJson(Object o, HttpServletResponse resp) throws IOException {
        String json = "";
        if (o instanceof String) {
            json = (String) o;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(o);
        }
        try (PrintWriter out = resp.getWriter()) {
            out.println(json);
        }
    }

    private void processModelAndView(Object result, HttpServletRequest req, HttpServletResponse resp,
            UrlMethod urlMethod) throws ServletException, IOException {
        ModelAndView mav = (ModelAndView) result;
        for (Map.Entry<String, Object> entry : mav.getValues().entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
        String view = mav.getView();
        if (view == null || view.isBlank()) {
            throw new ServletException("Aucune vue définie pour " + urlMethod);
        }
        String viewPath = prefix + view + suffix;
        RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
        dispatcher.forward(req, resp);
    }

    private Object parseParameter(String value, Class<?> type) {

        if (type == String.class) {
            return value;
        }

        if (type == int.class) {
            return Integer.parseInt(value);
        }

        if (type == long.class) {
            return Long.parseLong(value);
        }

        if (type == double.class) {
            return Double.parseDouble(value);
        }

        if (type == float.class) {
            return Float.parseFloat(value);
        }

        if (type == short.class) {
            return Short.parseShort(value);
        }

        if (type == byte.class) {
            return Byte.parseByte(value);
        }

        if (type == boolean.class) {
            return Boolean.parseBoolean(value);
        }

        if (type == char.class) {
            return value.charAt(0);
        }

        throw new IllegalArgumentException(
                "Type de paramètre non supporté : " + type.getName());
    }

    private void invokeSetter(Object o, Field attribut, Object val) {
        String attributeName = attribut.getName();
        String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
        try {
            Method m = o.getClass().getMethod(methodName, val.getClass());
            m.invoke(o, val);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleFileInput(HttpServletRequest req, FileMap m)
            throws Exception {

        Part fichier = req.getPart("fichier");

        if (fichier == null || fichier.getSize() == 0) {
            return;
        }

        String nomFichier = fichier.getSubmittedFileName();

        m.setNomFichier(nomFichier);

        int index = nomFichier.lastIndexOf('.');
        m.setExtension(
                index > 0 && index < nomFichier.length() - 1
                        ? nomFichier.substring(index + 1).toLowerCase()
                        : "");

        try (var input = fichier.getInputStream()) {
            m.setBin(input.readAllBytes());
        }

        Path chemin = FileUtils.sauvegarderFichier(m, uploadPath);

        System.out.println("Fichier enregistré : " + chemin);
    }

    private void parseFromRequest(HttpServletRequest req, Object o) throws Exception {
        Class<?> clazz = o.getClass();
        Field[] attributes = clazz.getDeclaredFields();
        System.out.println("Classe de o : " + o.getClass().getName());
        System.out.println("Classe : " + clazz.getName());

        for (Field f : attributes) {
            Class<?> fieldType = f.getType();
            if (fieldType.equals(FileMap.class)) {
                FileMap map = new FileMap();
                handleFileInput(req, map);
                invokeSetter(o, f, map);
                continue;
            }
            f.setAccessible(true);
            Class<?> attrType = f.getType();
            System.out.println("NOM DU PARAMETRE : " + f.getName() + " " + req.getParameter(f.getName()));
            invokeSetter(o, f, parseParameter(req.getParameter(f.getName()), attrType));
        }
    }

    private SessionFw getSession(HttpServletRequest req) {
        HttpSession httpSession = req.getSession();
        return sessions.computeIfAbsent(httpSession.getId(), id -> {
            SessionFw newSession = new SessionFw();
            SessionUtils.chargerSessionMap(newSession, httpSession);
            return newSession;
        });
    }

    private boolean isAuthenticated(HttpServletRequest req) {
        if (authSessionAttribute == null || authSessionAttribute.isBlank()) {
            return false;
        }

        HttpSession httpSession = req.getSession(false);
        return httpSession != null && httpSession.getAttribute(authSessionAttribute) != null;
    }

    private boolean checkAuthentication(Method controllerMethod, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        if (!controllerMethod.isAnnotationPresent(RequireAuth.class) || isAuthenticated(req)) {
            return true;
        }

        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise");
        return false;
    }

    private Object[] initParams(Method method, HttpServletRequest req) throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Parameter[] parametersP = method.getParameters();
        Object[] parameters = new Object[parameterTypes.length];

        for (Parameter parameter : parametersP) {
            System.out.println("Nom = " + parameter.getName());
            System.out.println("Présent = " + parameter.isNamePresent());
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            if (paramType.equals(ApplicationContext.class)) {
                parameters[i] = springContext;
            } else if (paramType.isPrimitive()) {
                String name = parametersP[i].getName();

                String parameterVal = req.getParameter(name);

                parameters[i] = parseParameter(parameterVal, paramType);
            } else if (paramType.equals(SessionFw.class)) {
                parameters[i] = getSession(req);
            } else {
                System.out.println("TYPE DU PARAMETRE : " + parametersP[i].getType());
                Object obj = parametersP[i].getType().getConstructor().newInstance();
                parseFromRequest(req, obj);
                parameters[i] = obj;
            }
        }
        return parameters;
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        String httpMethod = req.getMethod();
        UrlMethod urlMethod = new UrlMethod(uri, httpMethod);

        Method controllerMethod = urlControllers.get(urlMethod);

        if (controllerMethod == null) {
            resp.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = resp.getWriter()) {
                out.println("URL invalide : " + urlMethod);
                out.println("URL valides :");
                for (UrlMethod key : urlControllers.keySet()) {
                    out.println(key);
                }
            } catch (IOException e) {
                log("Impossible d'écrire la réponse", e);
            }
            return;
        }

        try {
            if (!checkAuthentication(controllerMethod, req, resp)) {
                return;
            }

            Class<?> controllerClass = controllerMethod.getDeclaringClass();
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

            System.out.println("Paramètres : ");
            Object[] parameters = initParams(controllerMethod, req);
            SessionFw session = null;
            for (Object parameter : parameters) {
                if (parameter instanceof SessionFw) {
                    session = (SessionFw) parameter;
                    break;
                }
            }

            Object result = controllerMethod.invoke(controllerInstance, parameters);
            if (session != null) {
                SessionUtils.ecraserSession(session, req.getSession());
            }

            if (controllerMethod.isAnnotationPresent(Json.class)) {
                resp.setContentType("application/json");
                processJson(result, resp);
            }
            if (result instanceof ModelAndView) {
                processModelAndView(result, req, resp, urlMethod);
                return;
            }

            resp.setContentType("text/plain;charset=UTF-8");
            try (PrintWriter out = resp.getWriter()) {
                if (result instanceof String) {
                    out.println((String) result);
                } else {
                    out.println("URL trouvé : " + controllerClass.getName() + "." + controllerMethod.getName());
                }
                out.println("URL valides :");
                for (UrlMethod key : urlControllers.keySet()) {
                    out.println(key);
                }
            } catch (IOException e) {
                log("Impossible d'écrire la réponse", e);
            }

        } catch (Exception e) {
            log("Erreur lors du traitement de la requête " + urlMethod, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.println("Erreur interne : " + e.getMessage());
                e.printStackTrace();
            } catch (IOException io) {
                log("Impossible d'écrire l'erreur dans la réponse", io);
            }
        }
    }

}
