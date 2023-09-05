package de.scaramangado.lily.core.application;

import de.scaramangado.lily.core.annotations.LilyCommand;
import de.scaramangado.lily.core.annotations.LilyModule;
import de.scaramangado.lily.core.communication.Answer;
import de.scaramangado.lily.core.communication.Command;
import de.scaramangado.lily.core.exceptions.LilyRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
final class LilyAnnotationProcessor {

  private LilyAnnotationProcessor() {

  }

  static Map<String, Method> getAllLilyCommands(String rootPackage) {

    ClassLoader classLoader = LilyAnnotationProcessor.class.getClassLoader();

    InputStream classList = classLoader.getResourceAsStream(rootPackage.replaceAll("\\.", "/"));

    if (classList == null) {
      return Collections.emptyMap();
    }

    try (BufferedReader classReader = new BufferedReader(
        new InputStreamReader(classList)
    )) {
      List<? extends Class<?>> modules =
          classReader.lines()
                     .map(line -> getClass(rootPackage, line))
                     .filter(Objects::nonNull)
                     .filter(LilyAnnotationProcessor::isLilyModule)
                     .toList();

      return getAllLilyCommands(modules);
    } catch (Exception e) {
      LOGGER.error("Failed to find annotated classes", e);
      return Collections.emptyMap();
    }
  }

  private static boolean isLilyModule(Class<?> baseClass) {

    try {
      LilyModule annotation = baseClass.getAnnotation(LilyModule.class);
      return annotation != null;
    } catch (Exception e) {
      return false;
    }
  }

  private static Class<?> getClass(String packageName, String className) {

    String fullClassName = packageName + "." + className.split("\\.")[0];

    try {
      return Class.forName(fullClassName);
    } catch (Exception e) {
      LOGGER.error("Cannot load class {}", fullClassName, e);
      return null;
    }
  }

  static Map<String, Method> getAllLilyCommands(Collection<? extends Class<?>> classes) {

    Map<String, Method> methods = new HashMap<>();

    for (Class<?> clazz : classes) {

      Set<Method> annotatedMethods =
          Arrays.stream(clazz.getMethods())
                .filter(LilyAnnotationProcessor::isLilyCommand)
                .collect(Collectors.toSet());

      putAnnotatedMethods(methods, annotatedMethods);
    }

    return Collections.unmodifiableMap(methods);
  }

  private static boolean isLilyCommand(Method method) {

    try {
      LilyCommand command = method.getAnnotation(LilyCommand.class);
      return command != null;
    } catch (Exception e) {
      return false;
    }
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

    if (method.getReturnType() != Answer.class ||
        (method.getParameterCount() != 1 || method.getParameterTypes()[0] != Command.class)) {
      throw new LilyRuntimeException(String.format("Method %s.%s does not have a valid signature.",
                                                   method.getDeclaringClass().getCanonicalName(), method.getName()));
    }

    if (methods.get(command) != null) {
      throw new LilyRuntimeException(String.format("Command %s already exists.", command));
    }

    if (containsWhitespace(command)) {
      throw new LilyRuntimeException(String.format("Command '%s' contains whitespace.", command));
    }

    methods.put(command, method);
  }

  private static boolean containsWhitespace(String command) {

    return !command.matches("^\\S*$");
  }
}
