package com.medallia.merci.core.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link ConfigInstantiationException}.
 */
public class ConfigInstantiationExceptionTest {
    @Test
    public void testInstantiateException() {
        RuntimeException runtimeException = new RuntimeException();
        ConfigInstantiationException exception = new ConfigInstantiationException(runtimeException);
        Assert.assertSame(runtimeException, exception.getCause());
    }
}