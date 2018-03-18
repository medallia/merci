<!--
Copyright 2018 Medallia, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<img src="merci-logo.png" height="180px" />

Merci is a framework for feature flags and runtime configuration. It relies on an easy to learn, recursive JSON structure.

## Quick Start

This guide describes with examples how to use Merci.

### Using Merci in your Java<sup>(TM)</sup> application

Merci releases can be downloaded from the Maven central repository. Adding Merci to a Java<sup>(TM)</sup> application just requires adding Merci as a dependency to pom files.

```xml
<dependency>
    <groupId>com.medallia.merci</groupId>
    <artifactId>merci-core</artifactId>
    <version>0.1.0</version>
    <type>pom</type>
</dependency>
```

### Central Configuration Files

The core idea behind Merci is to use a small set of central, easy to read files with a recursive JSON or YAML structure. These files are fetched by Merci's configuration loader through a scheduled, aynchronous task.

The following example of a configuration file contains a feature flag called "enable-international-welcome". Its evaluation at runtime results in a true or false value, depending on the environment of the deployed application and the current user. The feature flag would only be true, which means active, if the current user is 'joe' and the environment of the deployed application instance is 'qa'.

```JSON
{
  "feature-flags": {
    "enable-international-welcome": {
      "value": false,
      "modifiers": {
        "type": "environment",
        "contexts": {
          "qa": {
            "value": false,
            "modifiers": {
              "type": "user",
              "contexts": {
                "joe": {
                  "value": true
                }
              }
            }
          },
          "production": {
            "value": false
          }
        }
      }
    }
  }
}
```

Runtime configurations - or short configs - share the same recursive structure but instead of boolean values, their values are objects, that are deserialized to immutable config objects.

```JSON
{
  "configs": {
    "com.medallia.merci.DBConfig": {
      "value": {
        "hosts": [ "invalid-host" ],
        "port": -1
      },
      "modifiers": {
        "type": "environment",
        "contexts": {
          "qa": {
            "value": {
              "hosts": [ "db1.somewhere.in.qa" ],
              "port": 9889
            }
          },
          "production": {
            "value": {
              "hosts": [ "db1.somewhere.in.prod", "db2.somewhere.in.prod", "db3.somewhere.in.prod" ],
              "port": 9889
            }
          }
        }
      }
    }
  }
}
```

### Initializing Merci
 
Merci's configuration loader, which is responsible for scheduling retrieval and processing of configuration changes, relies on a registered configuration fetcher to retrieve the latest configuration content from a local or remote source. The library provides a generic interface, that applications implement for fetching their configuration files. For testing purposes and for applications, which only read configurations from the local file system, Merci's Filesystem Configuration Fetcher class should be sufficient.

```Java
/**
 * Configuration fetcher for this application.
 */
public class MyAppConfigurationFetcher implements ConfigurationFetcher {
    @Override
    public Map<String, String> fetch(List<String> fileNames, String application) throws IOException {
        /* return new map with requested file names and latest configuration content for the provided application name. */
    }
}
```

Initializing Merci's components should follow the example below. Both configuration manager singletons, the feature flag manager and the config manager instances should be made available to the rest of the application code.

```Java
/* Initialize configuration fetcher. */
ConfigurationFetcher fetcher = new MyAppConfigurationFetcher(...);

/* Use Merci to initialize configuration managers and loader. */
Merci merci = new Merci(fetcher);

FeatureFlagManager featureFlagManager = merci.addFeatureFlagManager("myapp").registerFile("/featureflags.json").build();
ConfigManager configManager = merci.addConfigManager("myapp").registerFile("/configs.json").build();

ConfigurationLoader loader = merci.createLoader(Duration.ofSeconds(180));
loader.start();
```

### Toggling Features with Merci

Merci's feature flag manager allows developers to selectively enable and disable parts of their code without redeploying or restarting application instances. In the following code example, the execution path is determined by applying the runtime configuration context to the external definition of the "enable-international-welcome" feature flag.

```Java
public class HelloWorld {

    private final FeatureFlagManager featureFlagManager;

    public HelloWorld(FeatureFlagManager featureFlagManager) {
        this.featureFlagManager = featureFlagManager;
    }

    @GET
    public String get(@Context HttpServletRequest request) {
        ConfigurationContext context = ...;
        /* Evaluate, if feature flag is active based on provided runtime context. */
        if (featureFlagManager.isActive("enable-international-welcome", context)) {
            return new Translator().translate("Hello World!", request.getLocale().getLanguage());
        }
        return "Hello World!";
    }
```

### Using Merci for Runtime Configuration

Runtime configs are managed by a registered instance of Merci's config manager class. Using config objects of type HelloWorldConfig allows an external list of supported languages to be passed in at runtime.

```Java
public class HelloWorld {

    private final ConfigManager configManager;

    public HelloWorld(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @GET
    public String get(@Context HttpServletRequest request) {
        ConfigurationContext context = ...;
        /* Get config object based on provided runtime context. */
        HelloWorldConfig config = configManager.getConfig(HelloWorldConfig.class, context);
        if (config.isSupportedLanguage(request.getLocale().getLanguage())) {
            return new Translator().translate("Hello World!", request.getLocale().getLanguage());
        }
        return "Hello World!";
    }
    
    /** Config class. */
    public static class HelloWorldConfig {

        private final Set<String> languages;

        public HelloWorldConfig() {
            this(Collections.emptyList());
        }

        @JsonCreator
        public HelloWorldConfig(@JsonProperty("languages") List<String> languages) {
            this.languages = new HashSet<>(languages);
        }

        public boolean isSupportedLanguage(String language) {
            return languages.contains(language);
        }
    }
}
```
