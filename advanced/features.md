---
title: Features
caption: Creating Custom Feature  
category: advanced
keywords: plugins
permalink: /advanced/features.html
---

You can develop your own features and reuse them across all your Ktor applications, or you can share them with the community. A typical 
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

The `Configuration` instance is handed to the user installation script and allows the feature configuration. 

The `Feature` companion object conforms to Ktor API and connects things together.
 
A feature can be installed with the standard `install` function:

```kotlin
fun Application.main() {
    install(CustomFeature) { // Install a custom feature
        prop = "Hello" // configuration script
    }
}
```

See a complete example in the [custom feature sample](https://github.com/ktorio/ktor-samples/blob/master/feature/custom-feature/src/CustomHeader.kt)
