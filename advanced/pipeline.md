---
title: Pipeline
category: advanced
permalink: /advanced/pipeline.html
caption: Internal Pipeline Machinery
children: /advanced/pipeline/
---

{% include nomnoml-support.html %}

## Description

The pipeline is a structure containing a sequence of functions (blocks / lambdas) that are called one after another,
distributed in phases topologically ordered, with the ability to mutate the sequence and to call the remaining
functions in the pipeline and then get back to current block.
All the functions are suspending blocks/lambdas, thus the whole pipeline is asynchronous.

Since pipelines contains blocks of code, they can effectively be nested creating sub-pipelines.

Pipelines are being used in ktor as an extension mechanism to plug functionality at the right place.
For example, a Ktor application defines three main phases: Infrastructure, Call and Fallback.
And the routing feature defines its own nested pipeline inside the application's call phase.

## API

The simplified API of Pipelines (without some generics, only public members and no bodies), looks like this:

```kotlin
class PipelinePhase(val name: String)
class Pipeline <TSubject : Any, TContext : Any> {
    constructor(vararg phases: PipelinePhase)

    val attributes: Attributes
    
    fun addPhase(phase: PipelinePhase)
    fun insertPhaseAfter(reference: PipelinePhase, phase: PipelinePhase)
    fun insertPhaseBefore(reference: PipelinePhase, phase: PipelinePhase)
    
    fun intercept(phase: PipelinePhase, block: suspend PipelineContext.(TSubject) -> Unit)
    
    fun merge(from: Pipeline)

    suspend fun execute(context: TContext, subject: TSubject): TSubject
}
```  

## Phases

Phases are groups of interceptors that can be ordered topologically defining relations between them.

You can define your own pipeline phases like this:

```kotlin
val myPipelinePhase = PipelinePhase("MyPipelinePhase")
```

You can register phases when constructing the pipeline: `Pipeline(phase1, phase2, phase3...)`

You can also register your phase later in a pipeline by calling the `Pipeline.addPhase` method.
Or register defining a relation to another phase adjusting the order of the phases by using the
`Pipeline.insertPhaseAfter` and `Pipeline.insertPhaseBefore` methods defining relations for phases
to be topologically sorted.

For example, if you define two phases, and want them to be executed in order, you can:
```kotlin
val phase1 = PipelinePhase("MyPhase1")
val phase2 = PipelinePhase("MyPhase2")
pipeline.insertPhaseAfter(ApplicationCallPipeline.Infrastructure, phase1)
pipeline.insertPhaseAfter(phase1, phase2)
```

Then you can intercept phases, so your interceptors will be called in that phase, in the order they are registered:

```kotlin
pipeline.intercept(phase1) { println("Phase1[A]") }
pipeline.intercept(phase2) { println("Phase2[A]") }
pipeline.intercept(phase2) { println("Phase2[B]") }
pipeline.intercept(phase1) { println("Phase1[B]") }
```

Would print:
```
Phase1[A]
Phase1[B]
Phase2[A]
Phase2[B]
```

You can execute the pipeline by calling the `execute` method providing a context and a subject:

```kotlin
pipeline.execute(context, subject)
```

You can omit calling the `addPhase` method when using the `insertPhase*` methods unless you need to register
a Phase that would be otherwise included by calling `Pipeline.merge` later.
<br/>
For example if you define a Phase inside a node in the routing feature, and then, in an inner node, try to insert
a phase using that one as reference, you would get an exception like `io.ktor.pipeline.InvalidPhaseException: Phase Phase('YourPhase') was not registered for this pipeline`.
<br/>
In this case you can just call `addPhase` so the phase is referenced before merging.
{: .note}

## Interceptors and the PipelineContext

When calling `Pipeline.intercept` you provide a phase where the interception will be appended,
and you also have to provide a function/lambda that receives a `this: PipelineContext`,
so you can handle the whatever you need. Inside that context, you have access to a proper typed Context (usually the `ApplicationCall`)
and an optional `Subject` so you can pass information around to other interceptors.
Then there are three methods to control how is going the pipeline to continue the execution. 

The context API:

```kotlin
class PipelineContext<TSubject : Any, out TContext : Any>() {
    val context: TContext
    val subject: TSubject
    
    fun finish()
    suspend fun proceedWith(subject: TSubject): TSubject
    suspend fun proceed(): TSubject
}
```

This way, the interceptors can control the flow in the following ways:

* Throwing an exception: Exception propagates back, and the pipeline is canceled.
* Calling the `proceed` or `proceedWith` functions: The interceptor is suspended, while the rest of the pipeline is executed, and once completed then function is resumed and the code after calling `proceed`/`proceedWith` is executed.
* Calling the `finish()` function: The pipeline finishes without any exceptions and without executing the rest of the pipeline
* In other case: the next function is called, or the pipeline finishes if it was last function.

The order of the blocks is determined by the order of phases they are installed into, then by installation order.
Phases are defined when the pipeline is created, and can be augmented to add more phases using `pipeline.phases`

For `PipelineContext` that has an `ApplicationCall` as context, there is a convenience extension property `call`
as an alias to `context`.
{: .note}

## The Subject

When executing, the pipeline context also carries a _subject_: an arbitrary typed generic object `TSubject` that is being _processed_.

The `subject` is accessible from the context as a property with its own name. And it is propagated between interceptors.
You can change the instance (for example for immutable subjects) using the `PipelineContext.proceedWith(subject)` method.
When using this method, the pipeline will continue with the new subject instance and will return to the caller with
the last instance passed by the pipeline thus effectively allowing to process the subject in later interceptions.

For a pipeline without a subject you can use `Unit`, for example, since the `ApplicationCallPipeline` doesn't require
a subject, it use uses Unit.
{: .note}

## Merging  

Pipelines of the same type can be merged. This is done with `merge` function on a receiving a pipeline. 
All interceptors from merging the pipeline are added to the receiving pipeline according to their phases.  
Pipelines are being merged when there are different points where interceptors can be installed.

One example is the response
pipelines that can be intercepted on the application level, on call level, or per route; before we execute a response
pipeline we merge them all.

## Ktor pipelines

### ApplicationCallPipeline
{: #ApplicationCallPipeline }

Ktor defines a pipeline without subject, and the `ApplicationCall` as context
defining three phases `Infrastructure`, `Call` and `Fallback` to be executed in that order:

<div class="nomnoml">
#direction: right
#.call: fill=#af8
#.fallback: fill=#faa dashed
[&lt;call&gt;Call]
[&lt;fallback&gt;Fallback]
[Infrastructure] then -> [Call]
[Call] then -> [Fallback]
</div>

The code looks like this:


```
open class ApplicationCallPipeline : Pipeline<Unit, ApplicationCall>(Infrastructure, Call, Fallback) {
    val receivePipeline = ApplicationReceivePipeline()
    val sendPipeline = ApplicationSendPipeline()

    companion object {
        val Infrastructure = PipelinePhase("Infrastructure")
        val Call = PipelinePhase("Call")
        val Fallback = PipelinePhase("Fallback")
    }
}
```

This base pipeline is used by the `Application` and the `Routing` feature.

The idea is that generic features that do not complete the call, like the [Authentication](/features/authentication.html) feature, intercept the `Infrastructure`,
while things like the [Routing](/features/routing.html) feature that are going to complete the call, goes to the `Call`,
and Features, like the [StatusPages], that must process unhandled calls and resolve them somehow, intercept the `Fallback` phase.

[StatusPages]: /features/status-pages.html

### Application

A ktor `Application` is a `ApplicationCallPipeline`. This is the main pipeline used for web backend
applications handling http requests.

### Routing

The [routing feature](features/routing.html) defines a nested pipeline attached to the Call phase in the
Application pipeline.
You can get the routing root node pipeline by calling `val routing = application.routing {}`.
Each node in the Route tree defines its own pipeline that is later merged per each route.

By merging the tree pipelines, you can define phases and interceptions at some point in the tree, and then
they will be executed in the order defined by the phase relations.

Since Route nodes has its own pipeline and the merge happens later, if you plan to add relations to some phases
defined in other ancestor Route nodes, you will have to add them with `Pipeline.addPhase` in the specific Route node
to avoid the `io.ktor.pipeline.InvalidPhaseException: Phase Phase('YourPhase') was not registered for this pipeline` exception.
{: .note}

### Other

Ktor also define other pipelines in some other features.

## Samples   

See [`PipelineTest`](https://github.com/ktorio/ktor/blob/master/ktor-utils/test/io/ktor/tests/utils/PipelineTest.kt) for examples.
