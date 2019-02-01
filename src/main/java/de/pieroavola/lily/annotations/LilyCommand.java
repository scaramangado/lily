package de.pieroavola.lily.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method is invoked as a chat command.
 *
 * Such a method should receive an array of String and return a single String.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LilyCommand {

    /**
     * The list of commands that invoke the method.
     *
     * A command may only consist of letters and numbers.
     *
     * @return List of commands.
     */
    String[] value();
}
