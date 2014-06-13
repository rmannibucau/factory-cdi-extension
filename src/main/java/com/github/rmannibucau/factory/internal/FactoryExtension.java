package com.github.rmannibucau.factory.internal;

import com.github.rmannibucau.factory.Factory;
import com.github.rmannibucau.factory.InstanceFactory;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBean;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class FactoryExtension implements Extension {
    private final Set<Type> neededTypes = new HashSet<Type>();

    <T> void findTypes(final @Observes ProcessBean<T> pat) {
        for (final InjectionPoint field : pat.getBean().getInjectionPoints()) {
            if (field.getAnnotated().isAnnotationPresent(Factory.class)) {
                neededTypes.add(field.getType());
            }
        }
    }

    void addBeans(final @Observes AfterBeanDiscovery abd, final BeanManager bm) {
        if (neededTypes.isEmpty()) {
            return;
        }

        for (final Type c : neededTypes) {
            final Class clazz = Class.class.cast(c);
            final Bean<Object> bean = new BeanBuilder<Object>(bm)
                    .passivationCapable(Serializable.class.isAssignableFrom(clazz))
                    .beanClass(clazz)
                    .scope(Dependent.class)
                    .types(Object.class, c)
                    .qualifiers(new AnyLiteral(), FactoryQualifier.QUALIFIER)
                    .beanLifecycle(new ContextualLifecycle<Object>() {
                        private volatile InstanceFactory factory = null;
                        private volatile CreationalContext<?> factoryContext = null;

                        @Override
                        public Object create(final Bean<Object> bean, final CreationalContext<Object> creationalContext) {
                            return BeanProvider.getContextualReference(InstanceFactory.class).instance(clazz);
                        }

                        @Override
                        public void destroy(final Bean<Object> bean, final Object instance, final CreationalContext<Object> creationalContext) {
                            // no-op
                        }
                    })
                    .create();
            abd.addBean(bean);
        }

        neededTypes.clear();
    }
}
