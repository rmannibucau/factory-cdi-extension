package com.github.rmannibucau.factory;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class FactoryExtensionTest {
    @Inject
    @Factory
    private Component component;

    @Test
    public void run() {
        assertNotNull(component);
        assertEquals(Component.class.getName(), component.name);
    }

    @Module
    @Classes(value = { SimpleFactory.class, Component.class, FactoryExtensionTest.class }, cdi = true)
    public EjbJar jar() {
        return new EjbJar();
    }

    public static class Component {
        private final String name;

        public Component(final String name) {
            this.name = name;
        }

        public Component() {
            // ensure it is a CDI bean too so no ambiguity surprise
            name = null;
        }
    }

    public static class SimpleFactory implements InstanceFactory {
        @Override
        public <T> T instance(final Class<T> type) {
            return type.cast(new Component(type.getName()));
        }
    }
}
