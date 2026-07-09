package framework.utils;

import java.util.Map;

public class ModelAndView {
    private String view ;
    private Map<String, Object> values;
    public String getView() {
        return view;
    }
    public void setView(String view) {
        this.view = view;
    }
    public Map<String, Object> getValues() {
        return values;
    }
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    
    
}
