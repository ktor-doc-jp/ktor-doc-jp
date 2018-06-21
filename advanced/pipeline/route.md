---
title: Route interception
category: advanced
caption: Interception per route
---

If you want to just intercept some calls for a specific route, you have to create a Route node and intercept that node.

For example, for creating a timeout for a route, you could do the following:

```kotlin
fun Route.routeTimeout(time: Long, unit: TimeUnit = TimeUnit.SECONDS, callback: Route.() -> Unit): Route {
    // With createChild, we create a child node for this received Route  
    val routeWithTimeout = this.createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    // Intercepts calls from this route at the infrastructure step
    routeWithTimeout.intercept(ApplicationCallPipeline.Infrastructure) {
        withTimeout(time, unit) {
            proceed() // With proceed we can define code to be executed before and after the call
        }
    }
    
    // Configure this route with the block provided by the user
    call(routeWithTimeout)

    return routeWithTimeout
}
```

### Getting the route being handled
{: #route-from-call }

You can get the route being handled by casting the `call: ApplicationCall` to `RoutingApplicationCall` that 
has a `route: Route` property.

### Getting the route path
{: #route-path }

`Route` overrides the `toString()` to generate a path to the route of the toString, looking something like:

```kotlin
override fun Route.toString() = when {
    parent == null -> "/"
    parent.parent == null -> "/$selector"
    else -> "$parent/$selector"
}
```

### Hooking before and after routing 

You can globally intercept the routing calls by using the events `Routing.RoutingCallStarted` and `Routing.RoutingCallFinished`:

```kotlin
pipeline.environment.monitor.subscribe(Routing.RoutingCallStarted) { call: RoutingApplicationCall ->
    println("Route started: ${call.route}")
}

pipeline.environment.monitor.subscribe(Routing.RoutingCallFinished) { call: RoutingApplicationCall ->
    println("Route completed: ${call.route}")
}
```

You can see a full example of this in the [Metrics feature](https://github.com/ktorio/ktor/blob/master/ktor-features/ktor-metrics/src/io/ktor/metrics/Metrics.kt).
