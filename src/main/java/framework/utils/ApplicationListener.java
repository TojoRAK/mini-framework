package framework.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import framework.annotation.Controller;

public class ApplicationListener implements ServletContextListener {

    private static String requiredInitParameter(ServletContext servletContext, String name) {
        String value = servletContext.getInitParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Le paramètre obligatoire '" + name + "' est absent ou vide dans web.xml");
        }
        return value.trim();
    }

    private static String requiredProperty(String name) throws Exception {
        String value = PropertiesReader.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "La propriété obligatoire '" + name + "' est absente ou vide dans application.properties");
        }
        return value.trim();
    }


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {

            List<String> listController = new ArrayList<>();
            Map<UrlMethod, Method> urlControllers = new HashMap<>();
            Map<Class<?>, Object> controllerInstances = new ConcurrentHashMap<>();

            ServletContext sc = sce.getServletContext();

            Map<String, SessionFw> sessions = new ConcurrentHashMap<>();
            sc.setAttribute("sessions", sessions);

            String packageName = requiredInitParameter(sc, "controller");
            for (Class<?> clazz : AnnotationFinder.findClassWithAnnotation(Controller.class, packageName)) {
                listController.add(clazz.getName());
                AnnotationFinder.findUrls(clazz, urlControllers);
            }
            if (listController.isEmpty()) {
                throw new IllegalStateException(
                        "Aucun contrôleur annoté @Controller n'a été trouvé dans le package '"
                                + packageName + "'");
            }
            String prefix = requiredInitParameter(sc, "view.prefix");
            String suffix = requiredInitParameter(sc, "view.suffix");
            String authSessionAttribute = requiredInitParameter(sc, "auth.session.attribute");
            String roleSessionAttribute = requiredInitParameter(sc, "role-session-attribute");

            System.out.println("AUTH-SESSION-ATTRIBUTE =========== " + authSessionAttribute);

            sc.setAttribute("listController", listController);
            sc.setAttribute("urlControllers", urlControllers);
            sc.setAttribute("authSessionAttribute", authSessionAttribute);
            sc.setAttribute("roleSessionAttribute", roleSessionAttribute);
            sc.setAttribute("controllerInstances", controllerInstances);
            
            String uploadLocation = requiredProperty("upload-path");

            sc.setAttribute("uploadLocation", uploadLocation);
            System.out.println(
                    "Listener : " + sc.getAttribute("urlControllers"));
            sc.setAttribute("prefix", prefix);
            sc.setAttribute("suffix", suffix);
            ApplicationContext springContext = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(sce.getServletContext());
            sc.setAttribute("springContext", springContext);

        } catch (Exception e) {
            throw new IllegalStateException("Échec de l'initialisation de l'application", e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
