package com.medallia.merci.core.utils;

/**
 * Interface for finding Java classes based on their names.
 *
 * @param <T> Base type of class.
 */
public interface ClassFinder<T> {
    /**
     * Return Java class based on provided name.
     *
     * @param className name for class lookup
     * @return class class for given name
     * @throws ClassNotFoundException, if no class could be found with provided name
     */
    Class<T> findClass(String className) throws ClassNotFoundException;
}
