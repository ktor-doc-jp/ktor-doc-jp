---
title: Environments
caption: How to differentiate between environments
category: servers
---

You might want to do different things depending on whether your server is running locally or in your
production machine.

Ktor doesn't impose any way for doing this, but here we provide some guidelines you can use in the
case you don't have a strong opinion about it.

### HOCON & ENV
{: #proposal }

You can use the `application.conf` file to set a variable that will hold the environment, then check that variable
at runtime and decide what to do.
You can configure to check an environment variable `KTOR_ENV` and to provide a default value `dev`.
Then in production you set the `KTOR_ENV=prod`

For example:

**application.conf:**

```
ktor {
    environment = dev
    environment = ${?KTOR_ENV}
}
```

You can access this config from the application, and use some extension properties to make your life easier:

```kotlin
fun Application.module() {
    when {
        isDev -> {
            // Do things only in dev   
        }
        isProd -> {
            // Do things only in prod
        }
    }
    // Do things for all the environments
}

val Application.envKind get() = environment.config.property("ktor.environment").getString()
val Application.isDev get() = envKind == "dev"
val Application.isProd get() = envKind != "dev"
```
