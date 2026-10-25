package framework.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import framework.annotation.Controller;

public class ApplicationListener implements ServletContextListener {

    private static final String AUTH_SESSION_ATTRIBUTE_PARAM = "";
    private static final String AUTH_SESSION_ATTRIBUTE_PROPERTY = "auth-session-attribute";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {

            List<String> listController = new ArrayList<>();
            Map<UrlMethod, Method> urlControllers = new HashMap<>();
            ServletContext sc = sce.getServletContext();

            Map<String, SessionFw> sessions = new HashMap<>();
            sc.setAttribute("sessions", sessions);

            String packageName = sc.getInitParameter("controller");
            for (Class<?> clazz : AnnotationFinder.findClassWithAnnotation(Controller.class, packageName)) {
                listController.add(clazz.getName());
                AnnotationFinder.findUrls(clazz, urlControllers);
            }
            String prefix = sc.getInitParameter("view.prefix");
            String suffix = sc.getInitParameter("view.suffix");
            String authSessionAttribute = sc.getInitParameter("auth-session-attribute");
            if (authSessionAttribute == null || authSessionAttribute.isBlank()) {
                authSessionAttribute = PropertiesReader.getProperty(AUTH_SESSION_ATTRIBUTE_PROPERTY);
            }

            sc.setAttribute("listController", listController);
            sc.setAttribute("urlControllers", urlControllers);
            sc.setAttribute("authSessionAttribute", authSessionAttribute);
            
            String uploadLocation = PropertiesReader.getProperty("upload-path");

            sc.setAttribute("uploadLocation", uploadLocation);
            System.out.println(
                    "Listener : " + sc.getAttribute("urlControllers"));
            sc.setAttribute("prefix", prefix);
            sc.setAttribute("suffix", suffix);
            ApplicationContext springContext = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(sce.getServletContext());
            sc.setAttribute("springContext", springContext);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
