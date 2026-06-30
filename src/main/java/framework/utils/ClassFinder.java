package framework.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ClassFinder {

    public static List<Class<?>> findClasses(String packageName)
            throws ClassNotFoundException, IOException {

        String packagePath = packageName.replace('.', '/');
        File dossier = null;
        try {
            dossier = new File(Thread.currentThread().getContextClassLoader().getResource(packagePath).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return findClasses(dossier, packageName);
    }

    public static List<Class<?>> findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file, packageName+"."+file.getName()));
                } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                    String className = packageName + '.' + file.getName().replace(".class", "");
                    classes.add(classLoader.loadClass(className));
                }
            }
        }
        return classes;
    }
}