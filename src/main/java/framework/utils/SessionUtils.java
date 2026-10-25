package framework.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

public final class SessionUtils {

    private SessionUtils() {
    }

    public static void chargerSessionMap(SessionFw sf, HttpSession session) {
        List<String> attributes = Collections.list(session.getAttributeNames());
        for (String attr : attributes) {
            sf.getSessionMap().put(attr, session.getAttribute(attr));
        }
    }

    public static void ecraserSession(SessionFw sf, HttpSession session) {
        List<String> attributes = Collections.list(session.getAttributeNames());
        for (String attr : attributes) {
            if (!sf.getSessionMap().containsKey(attr)) {
                session.removeAttribute(attr);
            }
        }

        for (Map.Entry<String, Object> entry : sf.getSessionMap().entrySet()) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
    }
}
