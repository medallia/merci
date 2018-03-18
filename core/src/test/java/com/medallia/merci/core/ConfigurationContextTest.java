package com.medallia.merci.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * Unit tests for {@link ConfigurationContext}.
 */
public class ConfigurationContextTest {

    @Test
    public void testGetReturnsValueAfterPut()  {
        ConfigurationContext runtimeContext = new ConfigurationContext();
        runtimeContext.put("environment", "qa");
        Assert.assertEquals("qa", runtimeContext.get("environment"));
    }

    @Test
    public void testEquals() {
        ConfigurationContext runtimeContext1 = new ConfigurationContext();
        runtimeContext1.put("environment", "qa");
        Assert.assertTrue(runtimeContext1.equals(runtimeContext1));
        Assert.assertFalse(runtimeContext1.equals(null));
        Assert.assertFalse(runtimeContext1.equals(new HashMap()));
        ConfigurationContext runtimeContext2 = new ConfigurationContext();
        runtimeContext2.put("environment", "qa");
        Assert.assertTrue(runtimeContext1.equals(runtimeContext2));
        ConfigurationContext runtimeContext3 = new ConfigurationContext();
        runtimeContext3.put("environment", "prod");
        Assert.assertFalse(runtimeContext1.equals(runtimeContext3));

    }

    @Test
    public void testHashCode() {
        ConfigurationContext runtimeContext1 = new ConfigurationContext();
        runtimeContext1.put("environment", "qa");
        ConfigurationContext runtimeContext2 = new ConfigurationContext();
        runtimeContext2.put("environment", "qa");
        Assert.assertEquals(runtimeContext1.hashCode(), runtimeContext2.hashCode());
    }
}