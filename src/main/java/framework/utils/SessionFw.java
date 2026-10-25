package framework.utils;

import java.util.HashMap;
import java.util.Map;

public class SessionFw {
    
    private Map<String, Object> sessionMap = new HashMap<>();

    
    public Object get(String key){
        return sessionMap.get(key);
    }
    public void add(String attr, Object o){
        this.sessionMap.put(attr, o);
    }
    public void remove(String attr){
        this.sessionMap.remove(attr);
    }

    public Map<String, Object> getSessionMap() {
        return sessionMap;
    }

    public void setSessionMap(Map<String, Object> sessionMap) {
        this.sessionMap = sessionMap;
    }
    
    


}
