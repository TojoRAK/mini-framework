package framework.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import framework.annotation.Json;
import framework.utils.AuthorizationChecker;
import framework.utils.ControllerInstanceManager;
import framework.utils.ModelAndView;
import framework.utils.RequestParameterResolver;
import framework.utils.ResponseRenderer;
import framework.utils.SessionFw;
import framework.utils.SessionUtils;
import framework.utils.UrlMethod;

@MultipartConfig
public class FrontController extends HttpServlet {

    private Map<UrlMethod, Method> urlControllers = new HashMap<>();
    private RequestParameterResolver parameterResolver;
    private AuthorizationChecker authorizationChecker;
    private ResponseRenderer responseRenderer;
    private ControllerInstanceManager controllerInstanceManager;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        // listController = (List<String>)
        // getServletContext().getAttribute("listController");
        System.out.println(
                "FrontController : " +
                        getServletContext().getAttribute("urlControllers"));
        urlControllers = (Map<UrlMethod, Method>) getServletContext().getAttribute("urlControllers");
        String prefix = (String) getServletContext().getAttribute("prefix");
        String suffix = (String) getServletContext().getAttribute("suffix");
        String uploadPath = (String) getServletContext().getAttribute("uploadLocation");
        ApplicationContext springContext = (ApplicationContext) getServletContext().getAttribute("springContext");
        Map<String, SessionFw> sessions = (Map<String, SessionFw>) getServletContext().getAttribute("sessions");
        String authSessionAttribute = (String) getServletContext().getAttribute("authSessionAttribute");
        String roleSessionAttribute = (String) getServletContext().getAttribute("roleSessionAttribute");
        Map<Class<?>, Object> controllerInstances = (Map<Class<?>, Object>) getServletContext()
                .getAttribute("controllerInstances");

        parameterResolver = new RequestParameterResolver(springContext, uploadPath, sessions);
        authorizationChecker = new AuthorizationChecker(authSessionAttribute, roleSessionAttribute);
        responseRenderer = new ResponseRenderer(prefix, suffix);
        controllerInstanceManager = new ControllerInstanceManager(controllerInstances);

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
            if (!authorizationChecker.checkAccess(controllerMethod, req, resp)) {
                return;
            }

            Class<?> controllerClass = controllerMethod.getDeclaringClass();
            
        
            Object controllerInstance = controllerInstanceManager.getInstance(controllerClass);

            System.out.println("Paramètres : ");
            Object[] parameters = parameterResolver.initParams(controllerMethod, req);
            SessionFw session = null;
            for (Object parameter : parameters) {
                if (parameter instanceof SessionFw) {
                    session = (SessionFw) parameter;
                    break;
                }
            }

            Object result = controllerMethod.invoke(controllerInstance, parameters);
            if (session != null) {
                SessionUtils.ecraserSession(session, req.getSession());
                System.out.println("======== SESSION ACTIVE =========");
            }

            if (controllerMethod.isAnnotationPresent(Json.class)) {
                resp.setContentType("application/json");
                responseRenderer.processJson(result, resp);
            }
            if (result instanceof ModelAndView) {
                responseRenderer.processModelAndView(result, req, resp, urlMethod);
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
                e.printStackTrace();
            } catch (IOException io) {
                log("Impossible d'écrire l'erreur dans la réponse", io);
            }
        }
    }

}
