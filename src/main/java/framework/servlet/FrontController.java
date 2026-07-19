package framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import framework.utils.ModelAndView;
import framework.utils.UrlMethod;

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
            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> paramType = parameterTypes[i];

                if (paramType.equals(ApplicationContext.class)) {
                    parameters[i] = springContext;
                } else {
                    parameters[i] = null;
                }
            }
            Object result = controllerMethod.invoke(controllerInstance, parameters);

            if (result instanceof ModelAndView) {
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