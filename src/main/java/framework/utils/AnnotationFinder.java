package framework.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import framework.annotation.Url;

public class AnnotationFinder {

    public static List<Class<?>> findClassWithAnnotation(Class<? extends Annotation> annotation, String packageName)
            throws Exception {
        List<Class<?>> retour = new ArrayList<>();

        for (Class<?> clazz : ClassFinder.findClasses(packageName)) {
            if (clazz.isAnnotationPresent(annotation)) {
                retour.add(clazz);
            }
        }
        return retour;
    }

    public static void findUrls(Class<?> clazz, Map<String, Method> map){
        Method[] methods = clazz.getMethods();

        for(Method method : methods){
            if(method.isAnnotationPresent(Url.class)){
                Url annotation = (Url) method.getAnnotation(Url.class);
                map.put(annotation.url(),method);
            }
        }
    }

}
