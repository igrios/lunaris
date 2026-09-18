package com.lunaris.ansenuza.infrastructure.persistence.entity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.EnumSet;
import java.util.UUID;
import org.hibernate.annotations.IdGeneratorType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Preserves defensive UUID assignments while supporting Hibernate-generated IDs. */
@IdGeneratorType(AssignedOrGeneratedUuid.Generator.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface AssignedOrGeneratedUuid {
    class Generator implements BeforeExecutionGenerator {
        @Override
        public Object generate(SharedSessionContractImplementor session, Object owner,
                Object currentValue, EventType eventType) {
            return currentValue == null ? UUID.randomUUID() : currentValue;
        }

        @Override
        public EnumSet<EventType> getEventTypes() {
            return EnumSet.of(EventType.INSERT);
        }

        @Override
        public boolean allowAssignedIdentifiers() {
            return true;
        }
    }
}
