package framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import framework.annotation.Controller;
import framework.utils.AnnotationFinder;


public class FrontController extends HttpServlet {

    private List<String> listController = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        try {
            for (Class<?> clazz : AnnotationFinder.findClassWithAnnotation(Controller.class, "controllers")) {
                listController.add(clazz.getName());
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
        String uri = req.getRequestURI();
        resp.setContentType("text/plain");

        try {
            PrintWriter out = resp.getWriter();
            out.println("URL : " + uri);
            out.println(listController.size());

            for (String controller : listController) {
                out.println(controller);
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }

    }

}