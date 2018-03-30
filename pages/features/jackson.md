---
title: Jackson
caption: JSON support using Jackson
section: Features
permalink: /features/jackson.html
feature:
    artifact: io.ktor:ktor-jackson:$ktor_version
    class: io.ktor.jackson.JacksonConverter
---

The Jackson feature allows you to handle JSON content in your application easily using
the [jackson](https://github.com/FasterXML/jackson) library.

This feature is a [ContentNegotiation](/features/content-negotiation.html) converter.

{% include feature.html %}

## Basic usage

To install the feature by registering a JSON content conversor using Jackson:

```kotlin
install(ContentNegotiation) {
    jackson {
        // Configure Jackson's ObjectMapper here
    }
}
```

The `jackson` block is a convenient method for:

```kotlin
register(ContentType.Application.Json, JacksonConverter(ObjectMapper().apply {
    // ...
}.create()))
```

## Configuration

Inside the `jackson` block, you have access to the [ObjectMapper](https://fasterxml.github.io/jackson-databind/javadoc/2.9/com/fasterxml/jackson/databind/ObjectMapper.html)
used to install the ContentNegotiation. To give you an idea of what is available:

```kotlin
install(ContentNegotiation) {
    jackson {
        this.enable(SerializationFeature.INDENT_OUTPUT)
        this.enable(...)
        this.dateFormat = DateFormat.getDateInstance()
        this.disableDefaultTyping()
        this.convertValue(..., ...)
        ...
    }
}
```
