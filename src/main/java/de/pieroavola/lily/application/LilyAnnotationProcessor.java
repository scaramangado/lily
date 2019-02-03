package de.pieroavola.lily.application;

import de.pieroavola.lily.annotations.LilyCommand;
import de.pieroavola.lily.annotations.LilyModule;
import de.pieroavola.lily.exceptions.LilyRuntimeException;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class LilyAnnotationProcessor {

    private LilyAnnotationProcessor() { }

    private static final Logger LOGGER = LoggerFactory.getLogger(LilyAnnotationProcessor.class);

    static Map<String, Method> getAllLilyCommands(String rootPackage) {

        return getAllLilyCommands(new Reflections(rootPackage).getTypesAnnotatedWith(LilyModule.class));
    }

    static Map<String, Method> getAllLilyCommands(Set<Class<?>> classes) {

        Map<String, Method> methods = new HashMap<>();

        for (Class clazz : classes) {

            Reflections reflection = new Reflections(
                    new ConfigurationBuilder()
                            .setUrls(ClasspathHelper.forClass(clazz))
                            .filterInputsBy(s -> s != null && s.startsWith(clazz.getCanonicalName()))
                            .setScanners(new MethodAnnotationsScanner()));

            Set<Method> annotatedMethods = reflection.getMethodsAnnotatedWith(LilyCommand.class);
            putAnnotatedMethods(methods, annotatedMethods);
        }

        return Collections.unmodifiableMap(methods);
    }

    private static void putAnnotatedMethods(Map<String, Method> methods, Set<Method> annotatedMethods) {

        for (Method method : annotatedMethods) {
            LilyCommand command = method.getAnnotation(LilyCommand.class);
            Arrays.asList(command.value()).forEach(s -> {
                try {
                    putIfValidCommand(s, method, methods);
                } catch (Exception e) {
                    LOGGER.error("A command to invoke the method {} cannot be bound.", method.getName());
                    LOGGER.debug("Exception", e);
                }
            });
        }
    }

    private static void putIfValidCommand(String command, Method method, Map<String, Method> methods) {

        if (method.getReturnType() != String.class ||
                (method.getParameterCount() != 1 || method.getParameterTypes()[0] != String[].class) ) {
            throw new LilyRuntimeException(String.format("Method %s.%s does not have a valid signature.",
                    method.getDeclaringClass().getCanonicalName(), method.getName()));
        }

        if (methods.get(command) != null) {
            throw new LilyRuntimeException(String.format("Command %s already exists.", command));
        }

        methods.put(command, method);
    }
}
