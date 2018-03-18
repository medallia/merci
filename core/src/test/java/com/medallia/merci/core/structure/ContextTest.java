/*
 * Copyright 2018 Medallia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.medallia.merci.core.structure;

import com.google.common.collect.ImmutableMap;
import com.medallia.merci.core.common.ClusterConfigurationContext;
import com.medallia.merci.core.common.EnvironmentConfigurationContext;
import com.medallia.merci.core.common.UserConfigurationContext;
import com.medallia.merci.core.ConfigurationContext;
import com.medallia.merci.core.configs.MessageConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link Context} and {@link Modifiers}.
 */
public class ContextTest {

    private final ConfigurationContext empty = new ConfigurationContext();
    private final ConfigurationContext qa = new EnvironmentConfigurationContext("qa");
    private final ConfigurationContext prod = new EnvironmentConfigurationContext("prod");
    private final ConfigurationContext cem341 = new ClusterConfigurationContext("qa", "cem341");
    private final ConfigurationContext cem1001 = new ClusterConfigurationContext("prod", "cem1001");
    private final ConfigurationContext joeInQA = new UserConfigurationContext("qa", "cem341", "joe");
    private final ConfigurationContext joeInProd = new UserConfigurationContext("prod", "cem1001", "joe");
    private final ConfigurationContext jackInQA = new UserConfigurationContext("qa", "cem341", "jack");

    /** Value of feature flag (Boolean) context is only true for user "joe" in environment "qa". */
    private final Context<Boolean> onlyTrueForJoeInQa = new Context<>(
            Boolean.FALSE,
            new Modifiers<>("environment", ImmutableMap.of(
                    "qa", new Context<>(
                            Boolean.FALSE,
                            new Modifiers<>("user", ImmutableMap.of(
                                    "joe", new Context<>(
                                            Boolean.TRUE,
                                            null)))),
                    "prod", new Context<>(
                            Boolean.FALSE,
                            null))));

    private final Context<MessageConfig> messageConfig = new Context<>(
            new MessageConfig("I just started."),
            new Modifiers<>("environment", ImmutableMap.of(
                    "qa", new Context<>(
                            new MessageConfig("I am almost there."),
                            new Modifiers<>("cluster", ImmutableMap.of(
                                    "cem341", new Context<>(
                                            new MessageConfig("Someone is testing in cem341."),
                                            new Modifiers<>("user", ImmutableMap.of(
                                                    "joe", new Context<>(
                                                            new MessageConfig("I am testing in cem341, Joe."),
                                                            null))))))),
                    "prod", new Context<>(
                            new MessageConfig("Yeah. I made it."),
                            null))));

    /**
     * Tests {@link Context} for a feature flag, that is true for any runtime configuration context.
     */
    @Test
    public void testContextForEnableFeatureAll() {
        /* Value of feature flag (Boolean) context is always true. */
        Context<Boolean> trueForAll = new Context<>(Boolean.TRUE, null);
        Assert.assertTrue(trueForAll.getValue(empty));
        Assert.assertTrue(trueForAll.getValue(qa));
        Assert.assertTrue(trueForAll.getValue(prod));
        Assert.assertTrue(trueForAll.getValue(cem341));
        Assert.assertTrue(trueForAll.getValue(cem1001));
        Assert.assertTrue(trueForAll.getValue(joeInQA));
        Assert.assertTrue(trueForAll.getValue(joeInProd));
        Assert.assertTrue(trueForAll.getValue(jackInQA));
    }

    /**
     * Tests {@link Context} for a feature flag, that is false for any runtime configuration context.
     */
    @Test
    public void testContextForEnableFeatureNone() {
        /* Value of feature flag (Boolean) context is always false. */
        Context<Boolean> falseForAll = new Context<>(Boolean.FALSE, null);
        Assert.assertFalse(falseForAll.getValue(empty));
        Assert.assertFalse(falseForAll.getValue(qa));
        Assert.assertFalse(falseForAll.getValue(prod));
        Assert.assertFalse(falseForAll.getValue(cem341));
        Assert.assertFalse(falseForAll.getValue(cem1001));
        Assert.assertFalse(falseForAll.getValue(joeInQA));
        Assert.assertFalse(falseForAll.getValue(joeInProd));
        Assert.assertFalse(falseForAll.getValue(jackInQA));
    }

    /**
     * Tests {@link Context} for a feature flag, that is active for any runtime configuration context.
     */
    @Test
    public void testContextForEnableFeatureOne() {
        Assert.assertFalse(onlyTrueForJoeInQa.getValue(empty));
        Assert.assertFalse(onlyTrueForJoeInQa.getValue(qa));
        Assert.assertFalse(onlyTrueForJoeInQa.getValue(prod));
        Assert.assertFalse(onlyTrueForJoeInQa.getValue(cem341));
        Assert.assertFalse(onlyTrueForJoeInQa.getValue(cem1001));
        Assert.assertFalse(onlyTrueForJoeInQa.getValue(joeInProd));
        Assert.assertFalse(onlyTrueForJoeInQa.getValue(jackInQA));
        Assert.assertTrue(onlyTrueForJoeInQa.getValue(joeInQA));
    }

    /**
     * Tests {@link Context} for {@link MessageConfig} objects.
     */
    @Test
    public void testContextForConfigOne() {
        /* Value of context of messageConfig depends on runtime (configuration) context. */
        Assert.assertEquals("I just started.", messageConfig.getValue(empty).getMessage());
        Assert.assertEquals("I am almost there.", messageConfig.getValue(qa).getMessage());
        Assert.assertEquals("Yeah. I made it.", messageConfig.getValue(prod).getMessage());
        Assert.assertEquals("Someone is testing in cem341.", messageConfig.getValue(cem341).getMessage());
        Assert.assertEquals("Yeah. I made it.", messageConfig.getValue(cem1001).getMessage());
        Assert.assertEquals("Yeah. I made it.", messageConfig.getValue(joeInProd).getMessage());
        Assert.assertEquals("Someone is testing in cem341.", messageConfig.getValue(jackInQA).getMessage());
        Assert.assertEquals("I am testing in cem341, Joe.", messageConfig.getValue(joeInQA).getMessage());
    }
}