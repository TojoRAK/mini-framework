package framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import framework.annotation.Controller;
import framework.utils.AnnotationFinder;

public class FrontController extends HttpServlet {

    private List<String> listController = new ArrayList<>();
    private Map<String, Method> urlControllers = new HashMap<>();

    @Override
    public void init() throws ServletException {
        String packageName = getServletConfig().getInitParameter("controller");
        try {
            for (Class<?> clazz : AnnotationFinder.findClassWithAnnotation(Controller.class, packageName)) {
                listController.add(clazz.getName());
                AnnotationFinder.findUrls(clazz,urlControllers); 
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
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
            PrintWriter out = resp.getWriter();
            out.println("Framework Personnalisé");
            out.println("URL : " + uri);
            // out.println(listController.size());
            Method correspondant = urlControllers.get(uri);
            
            for(String controller : listController){
                out.println(controller);
            }
            // for (Map.Entry<String, Method> entry : urlControllers.entrySet()) {
            //     out.print(entry.getKey());
            // }
            if (correspondant != null) {
                out.println("Controllers avec cette url : " + correspondant.getDeclaringClass().getName() + "."
                        + correspondant.getName());
            } else {
                out.println("Aucun URL correspondant.");
                out.println("URL Valides :");
                for (Map.Entry<String, Method> entry : urlControllers.entrySet()) {
                    out.print(entry.getKey());
                }
            }

        } catch (IOException e) {
            // e.printStackTrace();
        }

    }

}