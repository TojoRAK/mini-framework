package framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import framework.utils.ModelAndView;
import framework.utils.UrlMethod;

public class FrontController extends HttpServlet {

    private Map<UrlMethod, Method> urlControllers = new HashMap<>();
    private String prefix;
    private String suffix;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        // listController = (List<String>) getServletContext().getAttribute("listController");
        urlControllers = (Map<UrlMethod, Method>) getServletContext().getAttribute("urlControllers");
        prefix = (String) getServletContext().getAttribute("prefix");
        suffix = (String) getServletContext().getAttribute("suffix");
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
                out.println("Aucun URL correspondant pour : " + urlMethod);
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
            Object result = controllerMethod.invoke(controllerInstance);

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
                out.println("Framework Personnalisé");
                out.println("URL : " + uri);
                if (result instanceof String) {
                    out.println((String) result);
                } else if (result != null) {
                    out.println(result.toString());
                } else {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
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