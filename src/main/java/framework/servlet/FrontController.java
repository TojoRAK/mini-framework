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
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class FrontController extends HttpServlet {

    private List<String> listController = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .overrideClassLoaders(
                        Thread.currentThread().getContextClassLoader())
                .scan();) {

            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(Controller.class)) {
                listController.add(classInfo.getSimpleName());
            }
        }
    }

    @Override    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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