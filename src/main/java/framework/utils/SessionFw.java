package framework.utils;

import java.util.HashMap;
import java.util.Map;

public class SessionFw {

    private Map<String, Object> sessionMap = new HashMap<>();

    public Object get(String key) {
        return sessionMap.get(key);
    }

    public void add(String attr, Object value) {
        sessionMap.put(attr, value);
    }

    public void remove(String attr) {
        sessionMap.remove(attr);
    }

    public Map<String, Object> getSessionMap() {
        return sessionMap;
    }

    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
}
