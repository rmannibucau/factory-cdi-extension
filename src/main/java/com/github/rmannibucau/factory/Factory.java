package com.github.rmannibucau.factory;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Target({ CONSTRUCTOR, FIELD })
@Retention(RUNTIME)
public @interface Factory {
}
