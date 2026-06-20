package framework.utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class AnnotationFinder {
    
    public static List<Class<?>> findClassWithAnnotation(Class<?extends Annotation> annotation, String packageName) throws Exception{
        List<Class<?>> retour = new ArrayList<>();

        for(Class<?> clazz : ClassFinder.findClasses(packageName)){
            if(clazz.isAnnotationPresent(annotation)){
                retour.add(clazz);
            }
        }

        return retour;

        
    }



}
