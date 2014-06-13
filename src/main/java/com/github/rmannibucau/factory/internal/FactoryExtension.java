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
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class FactoryExtension implements Extension {
    private final Set<Class<?>> neededTypes = new HashSet<Class<?>>();

    <T> void findTypes(final @Observes ProcessAnnotatedType<T> pat) {
        for (final AnnotatedField<? super T> field : pat.getAnnotatedType().getFields()) {
            if (field.getAnnotation(Factory.class) != null) {
                neededTypes.add(field.getJavaMember().getType());
            }
        }
    }

    void addBeans(final @Observes AfterBeanDiscovery abd, final BeanManager bm) {
        if (neededTypes.isEmpty()) {
            return;
        }

        for (final Class<?> c : neededTypes) {
            final Bean<Object> bean = new BeanBuilder<Object>(bm)
                    .passivationCapable(Serializable.class.isAssignableFrom(c))
                    .beanClass(c)
                    .scope(Dependent.class)
                    .types(Object.class, c)
                    .qualifiers(new AnyLiteral(), FactoryQualifier.QUALIFIER)
                    .beanLifecycle(new ContextualLifecycle<Object>() {
                        private volatile InstanceFactory factory = null;
                        private volatile CreationalContext<?> factoryContext = null;

                        @Override
                        public Object create(final Bean<Object> bean, final CreationalContext<Object> creationalContext) {
                            return BeanProvider.getContextualReference(InstanceFactory.class).instance(c);
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
