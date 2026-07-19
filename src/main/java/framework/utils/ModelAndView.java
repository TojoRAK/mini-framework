package framework.utils;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String view ;
    private Map<String, Object> values = new HashMap<>();
    public String getView() {
        return view;
    }
    public void setView(String view) {
        this.view = view;
    }
    public void addObject(String key,Object o){
        values.put(key, o);
    }
    public Map<String, Object> getValues() {
        return values;
    }
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    
    
}
