package framework.utils;

import java.util.Map;

public class ControllerInstanceManager {

    private final Map<Class<?>, Object> controllerInstances;

    public ControllerInstanceManager(Map<Class<?>, Object> controllerInstances) {
        this.controllerInstances = controllerInstances;
    }

    public Object getInstance(Class<?> controllerClass) throws Exception {
        Object controllerInstance;
        if (controllerInstances.containsKey(controllerClass)) {
            controllerInstance = controllerInstances.get(controllerClass);
        } else {
            controllerInstance = controllerClass.getDeclaredConstructor().newInstance();
            controllerInstances.put(controllerClass, controllerInstance);
        }
        return controllerInstance;
    }
}
