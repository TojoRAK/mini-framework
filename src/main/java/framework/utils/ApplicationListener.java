package framework.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import framework.annotation.Controller;

public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
            List<String> listController = new ArrayList<>();
            Map<UrlMethod, Method> urlControllers = new HashMap<>();
            ServletContext sc = sce.getServletContext();
            String packageName = sc.getInitParameter("controller");
            for (Class<?> clazz : AnnotationFinder.findClassWithAnnotation(Controller.class, packageName)) {
                listController.add(clazz.getName());
                AnnotationFinder.findUrls(clazz, urlControllers);
            }
            String prefix = sc.getInitParameter("view.prefix");
            String suffix = sc.getInitParameter("view.suffix");

            sc.setAttribute("listController", listController);
            sc.setAttribute("urlControllers", urlControllers);
            sc.setAttribute("prefix", prefix);
            sc.setAttribute("suffix", suffix);
            sc.setAttribute("springContext", springContext);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
