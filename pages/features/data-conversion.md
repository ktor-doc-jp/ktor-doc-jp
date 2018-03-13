---
title: Data Conversion
caption: Data Conversion
section: Features
permalink: /features/data-conversion.html
feature:
    artifact: io.ktor:ktor-server-core:$ktor_version
    class: io.ktor.features.DataConversion
---

`DataConversion` is a feature that allows to serialize and deserialize list of values.

By default it handle primitive types and enums, but can be configured to handle additional types. 

If you are using the [Locations feature](/features/locations.html) and want to support
custom types as part of its parameters, you can add new custom converters with this
service.

**Table of contents:**

* TOC
{:toc}

{% include feature/feature.html %}

## Basic Installation

```kotlin
install(DataConversion)
```

## Adding Converters

The DataConversion configuration, provide a `convert` method to define
type conversions. For example:

```kotlin
install(DataConversion) {
    convert<Date> {
        val format = SimpleDateFormat.getInstance()
    
        decode { values, _ ->
            values.singleOrNull()?.let { format.parse(it) }
        }

        encode { value ->
            when (value) {
                null -> listOf()
                is Date -> listOf(SimpleDateFormat.getInstance().format(value))
                else -> throw DataConversionException("Cannot convert $value as Date")
            }
        }
    }
}
```

```kotlin
install(DataConversion)
```

## Accessing the Service

You can access the DataConversion service, from any call easily with:

```kotlin
val dataConversion = call.conversionService
```

## The ConversionService Interface

```kotlin
interface ConversionService {
    fun fromValues(values: List<String>, type: Type): Any?
    fun toValues(value: Any?): List<String>
}
```
