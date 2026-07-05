package framework.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import framework.annotation.Controller;

@WebListener
public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            List<String> listController = new ArrayList<>();
            Map<UrlMethod, Method> urlControllers = new HashMap<>();
            ServletContext sc = sce.getServletContext();
            String packageName = sc.getInitParameter("controller");
            for (Class<?> clazz : AnnotationFinder.findClassWithAnnotation(Controller.class, packageName)) {
                listController.add(clazz.getName());
                AnnotationFinder.findUrls(clazz, urlControllers);
            }
            sc.setAttribute("listController", listController);
            sc.setAttribute("urlControllers", urlControllers);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
