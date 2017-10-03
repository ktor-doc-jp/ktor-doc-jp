---
title: Features
tags: [advanced]
sidebar: mydoc_sidebar
permalink: /advanced/features.html
summary:  
---

You can develop your own features and reuse them across your Ktor applications, or share with the community. Typical 
feature has the following structure:

```kotlin
class CustomFeature(configuration: Configuration) {
    val prop = configuration.prop // get snapshot of config into immutable property
    
    class Configuration {
       var prop = "value" // mutable property
    }

    // implement ApplicationFeature in a companion object
    companion object Feature : ApplicationFeature<ApplicationCallPipeline, CustomFeature.Configuration, CustomFeature> {
       // create unique key for the feature
       override val key = AttributeKey<CustomFeature>("CustomFeature")
       
       // implement installation script
       override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): CustomFeature {
           
           // run configuration script
           val configuration = CustomFeature.Configuration().apply(configure)
           
           // create a feature 
           val feature = CustomFeature(configuration)
           
           // intercept a pipeline 
           pipeline.intercept(…) { 
                // call a feature
           }
           return feature
       }
    }
}
```

`CustomFeature` is a feature instance class, which should be immutable to avoid unintended side-effects in a highly
concurrent environment. Feature implementation should be thread-safe as it will be called from multiple threads.

`Configuration` instance is handed to the user installation script and allows for feature configuration. 

`Feature` companion object conforms to Ktor API and wires things together.
 
Feature can be installed with the standard `install` function:

```kotlin
fun Application.main() {
    install(CustomFeature) { // Install a custom feature
        prop = "Hello" // configuration script
    }
}
```

See complete example in a [custom feature sample](https://github.com/Kotlin/ktor/blob/master/ktor-samples/ktor-samples-custom-feature/src/org/jetbrains/ktor/samples/feature/CustomHeader.kt)
