package framework;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import framework.annotation.Controller;
import framework.tests.Personne;
import framework.utils.AnnotationFinder;

public class Main {

    private static void invokeSetter(Object o, Field attribut, Object val) {
        String attributeName = attribut.getName();
        String methodName = "set" + attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1);
        try {
            Method m = o.getClass().getMethod(methodName, val.getClass());
            m.invoke(o, val);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        List<Class<?>> classes = AnnotationFinder.findClassWithAnnotation(Controller.class, "framework");
        System.out.println(classes.size());
        for (Class<?> clazz : classes) {
            System.out.println(clazz.getName());
        }

        Personne p = new Personne();
        p.setNom("JJJ");
        p.setAge(12);
        p.setPrenom("ASDASDASD");

        System.out.println(p.getNom());
        Field attribut = p.getClass().getDeclaredField("nom");

        invokeSetter(p, attribut, "AAA");
        
        System.out.println(p.getNom());



    }

}
