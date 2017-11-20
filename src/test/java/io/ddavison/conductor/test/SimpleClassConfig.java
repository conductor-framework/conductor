package io.ddavison.conductor.test;

import io.ddavison.conductor.Browser;
import io.ddavison.conductor.Config;

import java.lang.annotation.Annotation;

public class SimpleClassConfig implements Config{
    @Override
    public Browser browser() {
        return Browser.SAFARI;
    }

    @Override
    public String path() {
        return "/buzz";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
