package wtf.saturn.util.reflect;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Uses reflection to gather fields/methods/annoations/etc
 *
 * @author aesthetical
 * @since 6/7/22
 */
public class ReflectionUtil {

    /**
     * Gets all fields with this type
     *
     * @param parent the parent class
     * @param fieldKlass the class the field has to be
     * @return a list of the fields with this type
     */
    public static List<Field> allFieldsWithType(Object parent, Class<?> fieldKlass) {
        return Arrays.stream(parent.getClass().getDeclaredFields())
                .filter((field) -> fieldKlass.isAssignableFrom(field.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all classes in a package
     *
     * @param path the package
     * @param klass the class type
     * @param <T> the type
     * @return a list of ClassInfo
     *
     * @throws IOException if something happens idk
     */
    public static <T> List<Class<T>> getClasses(String path, Class<T> klass) throws IOException {
        String fixedPackageName = path.replaceAll("[.]", "/");

        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(fixedPackageName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        List<Class<T>> classes = new CopyOnWriteArrayList<>();

        String t;
        while ((t = reader.readLine()) != null) {

            // this is a package, we'll go ahead and add all the classes from this package
            if (!t.endsWith(".class")) {
                classes.addAll(getClasses(path + "." + t, klass));
                continue;
            }

            Class<?> clazz = getClassByName(path, t);
            if (klass.isAssignableFrom(clazz)) {
                classes.add((Class<T>) clazz);
            }
        }

        return classes;
    }

    /**
     * Gets a class by its name
     * @param path the package the class should be in
     * @param name the name of the class
     * @return the class
     */
    @SneakyThrows
    public static Class<?> getClassByName(String path, String name) {
        return Class.forName(path + "." + name.substring(0, name.lastIndexOf(".")));
    }

    /**
     * If an annotation is present on this class
     *
     * @param parent the class to check
     * @param klass the annotation class
     * @return if the annotation is present
     */
    public static boolean hasAnnotation(Class<?> parent, Class<? extends Annotation> klass) {
        if (parent.isAnnotation() && parent.isAssignableFrom(klass)) {
            return true;
        }

        return parent.isAnnotationPresent(klass);
    }
}