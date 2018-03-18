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
package com.medallia.merci.core;

import com.google.common.collect.ImmutableMap;
import com.medallia.merci.core.common.EnvironmentConfigurationContext;
import com.medallia.merci.core.structure.Context;
import com.medallia.merci.core.structure.Modifiers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link Configuration}.
 */
public class ConfigurationTest {

    private final ConfigurationContext none = new ConfigurationContext();
    private final ConfigurationContext qa = new EnvironmentConfigurationContext("qa");
    private final ConfigurationContext prod = new EnvironmentConfigurationContext("prod");

    private final Context<Boolean> enableQA = new Context<>(
            Boolean.FALSE,
            new Modifiers<>("environment", ImmutableMap.of(
                    "qa", new Context<>(Boolean.TRUE,null),
                    "prod", new Context<>(Boolean.FALSE,null))));

    @Test
    public void testGetNameReturnsName() {
        String name = "enable-all";
        Context<Boolean> context = new Context<>(Boolean.TRUE, null);
        Configuration<Boolean> featureFlag = new Configuration<>(name, context);
        Assert.assertEquals(name, featureFlag.getName());
    }

    @Test
    public void testGetValueReturnsEvaluatedValue() {
        String name = "enable-one";
        Configuration<Boolean> featureFlag = new Configuration<>(name, enableQA);

        Assert.assertFalse(name, featureFlag.getValue(none));
        Assert.assertFalse(name, featureFlag.getValue(prod));
        Assert.assertTrue(name, featureFlag.getValue(qa));
    }

    @Test
    public void getContext() {
        String name = "enable-one";
        Configuration<Boolean> featureFlag = new Configuration<>(name, enableQA);

        Assert.assertFalse(name, featureFlag.getContext().getValue(none));
        Assert.assertFalse(name, featureFlag.getContext().getValue(prod));
        Assert.assertTrue(name, featureFlag.getContext().getValue(qa));
    }
}