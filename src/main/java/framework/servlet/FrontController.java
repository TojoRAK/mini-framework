package framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import framework.utils.ModelAndView;
import framework.utils.UrlMethod;

public class FrontController extends HttpServlet {

    private List<String> listController = new ArrayList<>();
    private Map<UrlMethod, Method> urlControllers = new HashMap<>();
    private String prefix;
    private String suffix;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        listController = (List<String>) getServletContext().getAttribute("listController");
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
        resp.setContentType("text/plain");
        try {
            String method = req.getMethod();

            UrlMethod urlMethod = new UrlMethod(uri, method);
            PrintWriter out = resp.getWriter();
            out.println("Framework Personnalisé");
            out.println("URL : " + uri);

            out.println(listController.size());
            Method correspondant = urlControllers.get(urlMethod);

            for (String controller : listController) {
                out.println(controller);
            }
            if (correspondant != null) {
                out.println("Controllers avec cette url : " + correspondant.getDeclaringClass().getName() + "."
                        + correspondant.getName());

                Class<?> clazz = correspondant.getDeclaringClass();
                try {
                    Object obj = clazz.getDeclaredConstructor().newInstance();
                    Object result = correspondant.invoke(obj);

                    if (result instanceof ModelAndView mav) {
                        if (mav.getValues() != null) {
                            req.setAttribute("map", mav.getValues());
                        }

                        if (mav.getView() != null && !mav.getView().isBlank()) {
                            String viewPath = prefix + mav.getView() + suffix;
                            RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                            dispatcher.forward(req, resp);
                            return;
                        }
                        throw new ServletException("Aucune vue définie pour " + urlMethod);
                    }
                    if (result instanceof String text) {
                        resp.setContentType("text/plain;charset=UTF-8");
                        out.println("Resultat de la methode:\n");
                        out.println(text);
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                out.println("Aucun URL correspondant.");
                out.println("URL Valides :");
                for (Map.Entry<UrlMethod, Method> entry : urlControllers.entrySet()) {
                    out.print(entry.getKey());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}