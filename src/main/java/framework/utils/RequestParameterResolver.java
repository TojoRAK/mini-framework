package framework.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.springframework.context.ApplicationContext;


public class RequestParameterResolver {

    private final ApplicationContext springContext;
    private final String uploadPath;
    private final Map<String, SessionFw> sessions;

    public RequestParameterResolver(ApplicationContext springContext, String uploadPath,
            Map<String, SessionFw> sessions) {
        this.springContext = springContext;
        this.uploadPath = uploadPath;
        this.sessions = sessions;
    }

    private Object parseParameter(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == int.class) return Integer.parseInt(value);
        if (type == long.class) return Long.parseLong(value);
        if (type == double.class) return Double.parseDouble(value);
        if (type == float.class) return Float.parseFloat(value);
        if (type == short.class) return Short.parseShort(value);
        if (type == byte.class) return Byte.parseByte(value);
        if (type == boolean.class) return Boolean.parseBoolean(value);
        if (type == char.class) return value.charAt(0);
        throw new IllegalArgumentException("Type de paramètre non supporté : " + type.getName());
    }

    private void invokeSetter(Object o, Field attribut, Object val) {
        String attributeName = attribut.getName();
        String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
        try {
            Method m = o.getClass().getMethod(methodName, val.getClass());
            m.invoke(o, val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFileInput(HttpServletRequest req, FileMap m, String attributeName) throws Exception {
        Part fichier = req.getPart(attributeName);
        if (fichier == null || fichier.getSize() == 0) return;

        String nomFichier = fichier.getSubmittedFileName();
        m.setNomFichier(nomFichier);
        int index = nomFichier.lastIndexOf('.');
        m.setExtension(index > 0 && index < nomFichier.length() - 1
                ? nomFichier.substring(index + 1).toLowerCase() : "");

        try (var input = fichier.getInputStream()) {
            m.setBin(input.readAllBytes());
        }
        Path chemin = FileUtils.sauvegarderFichier(m, uploadPath);
        System.out.println("Fichier enregistré : " + chemin);
    }

    private void parseFromRequest(HttpServletRequest req, Object o) throws Exception {
        Class<?> clazz = o.getClass();
        Field[] attributes = clazz.getDeclaredFields();
        System.out.println("Classe de o : " + o.getClass().getName());
        System.out.println("Classe : " + clazz.getName());

        for (Field f : attributes) {
            if (f.getType().equals(FileMap.class)) {
                FileMap map = new FileMap();
                handleFileInput(req, map, f.getName());
                invokeSetter(o, f, map);
                continue;
            }
            f.setAccessible(true);
            System.out.println("NOM DU PARAMETRE : " + f.getName() + " " + req.getParameter(f.getName()));
            invokeSetter(o, f, parseParameter(req.getParameter(f.getName()), f.getType()));
        }
    }

    private SessionFw getSession(HttpServletRequest req) {
        HttpSession httpSession = req.getSession();
        return sessions.computeIfAbsent(httpSession.getId(), id -> {
            SessionFw newSession = new SessionFw();
            SessionUtils.chargerSessionMap(newSession, httpSession);
            return newSession;
        });
    }

    public Object[] initParams(Method method, HttpServletRequest req) throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Parameter[] parametersP = method.getParameters();
        Object[] parameters = new Object[parameterTypes.length];

        for (Parameter parameter : parametersP) {
            System.out.println("Nom = " + parameter.getName());
            System.out.println("Présent = " + parameter.isNamePresent());
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            if (paramType.equals(ApplicationContext.class)) {
                parameters[i] = springContext;
            } else if (paramType.isPrimitive()) {
                parameters[i] = parseParameter(req.getParameter(parametersP[i].getName()), paramType);
            } else if (paramType.equals(SessionFw.class)) {
                System.out.println("======= SESSION ========");
                parameters[i] = getSession(req);
            } else {
                System.out.println("TYPE DU PARAMETRE : " + parametersP[i].getType());
                Object obj = parametersP[i].getType().getConstructor().newInstance();
                parseFromRequest(req, obj);
                parameters[i] = obj;
            }
        }
        return parameters;
    }
}
