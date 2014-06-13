package com.github.rmannibucau.factory.internal;

import com.github.rmannibucau.factory.Factory;

import javax.enterprise.util.AnnotationLiteral;

public class FactoryQualifier extends AnnotationLiteral<Factory> implements Factory {
    public static final FactoryQualifier QUALIFIER = new FactoryQualifier();

    @Override
    public boolean equals(final Object other) {
        return Factory.class.isInstance(other);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
