package com.medallia.merci.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Default implementation for {@link ClassFinder}.
 */
public class DefaultClassFinder implements ClassFinder<Object> {

    private final TypeFactory typeFactory;

    /**
     * Creates default class finder, using type factory from Jackson's object mapper.
     */
    public DefaultClassFinder() {
        this(new ObjectMapper().getTypeFactory());
    }

    /**
     * Creates default class finder with provided type factory.
     *
     * @param typeFactory type factory to be used for class lookup.
     */
    public DefaultClassFinder(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Object> findClass(String className) throws ClassNotFoundException {
        return (Class<Object>) typeFactory.findClass(className);
    }
}
