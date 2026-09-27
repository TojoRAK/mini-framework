package framework;

import java.util.List;

import framework.annotation.Controller;
import framework.utils.AnnotationFinder;

public class Main {
    
    
    public static void main(String[] args) throws Exception{
        List<Class<?>> classes = AnnotationFinder.findClassWithAnnotation(Controller.class,"framework");
        System.out.println(classes.size());
        for(Class<?> clazz : classes){
            System.out.println(clazz.getName());
        }
    }

    
}
