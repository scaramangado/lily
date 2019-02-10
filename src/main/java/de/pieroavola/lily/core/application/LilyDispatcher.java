package de.pieroavola.lily.core.application;

import de.pieroavola.lily.core.annotations.LilyModule;
import de.pieroavola.lily.core.communication.Dispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
class LilyDispatcher implements Dispatcher {

    private final ApplicationContext ac;

    private Map<String, Method> commands = null;

    LilyDispatcher(@Autowired ApplicationContext ac) {
        this.ac = ac;
    }

    @Override
    public Optional<String> dispatch(String command, String[] args) {

        if(commands == null) {
            initializeCommands();
        }

        Method method = commands.get(command);

        try {
            return Optional.of((String) method.invoke(ac.getBean(method.getDeclaringClass()), (Object) args));
        } catch (NullPointerException e) {
            // Command not defined.
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return Optional.empty();
    }

    private void initializeCommands() {
        Collection<Object> beans = ac.getBeansWithAnnotation(LilyModule.class).values();
        commands = LilyAnnotationProcessor.getAllLilyCommands(beans
                .stream().map(Object::getClass).collect(Collectors.toSet()));
    }
}
