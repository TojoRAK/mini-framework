package framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import framework.annotation.Json;
import framework.utils.ModelAndView;
import framework.utils.UrlMethod;
// import tools.jackson.databind.ObjectMapper;

public class FrontController extends HttpServlet {

    private Map<UrlMethod, Method> urlControllers = new HashMap<>();
    private String prefix;
    private String suffix;
    private ApplicationContext springContext;

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
        springContext = (ApplicationContext) getServletContext().getAttribute("springContext");
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
            Class<?> controllerClass = controllerMethod.getDeclaringClass();
            Object controllerInstance = controllerClass.getDeclaredConstructor().newInstance();

            Class<?>[] parameterTypes = controllerMethod.getParameterTypes();
            Parameter[] parametersP = controllerMethod.getParameters();

            for (Parameter parameter : parametersP) {
                System.out.println("Nom = " + parameter.getName());
                System.out.println("Présent = " + parameter.isNamePresent());
            }
            Object[] parameters = new Object[parameterTypes.length];
            System.out.println("Paramètres : ");

            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> paramType = parameterTypes[i];
                if (paramType.equals(ApplicationContext.class)) {
                    parameters[i] = springContext;
                } else {
                    String name = parametersP[i].getName();

                    String parameterVal = req.getParameter(name);

                    parameters[i] = parseParameter(parameterVal, paramType);
                }
            }

            Object result = controllerMethod.invoke(controllerInstance, parameters);

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
            } catch (IOException io) {
                log("Impossible d'écrire l'erreur dans la réponse", io);
            }
        }
    }

}