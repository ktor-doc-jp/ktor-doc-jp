---
title: Pipeline
section: Advanced
permalink: /advanced/pipeline.html
caption: Internal Pipeline Machinery
---

Pipeline is a sequence of blocks(lambdas) that are called one after another, with the ability to extend the list
and call remaining pipeline and then get back to current function. All blocks are suspending functions/lambdas, thus
the whole pipeline is async

```
[block1] -> [block2] -> [block3]
```

Block can run in one of few ways:

* run to the end. In this case next block is called, or pipeline finishes if it was last block
* throw exception. exception propagates back and pipeline is cancelled
* call `proceed(…)` function. The rest of pipeline is executed and then code after `proceed` is executed
* call `finish()` function. Pipeline finishes without an exception and without executing rest of pipeline

Pipeline is filled with blocks using `intercept` function.
Pipeline is executed using `execute` function.

Order of blocks is determined by the order of phases they are installed into, then by installation order.
Phases are defined when pipeline is created, and can be augmented to add more phases using `pipeline.phases`

Pipeline is bound to an `ApplicationCall` that initiated its execution in the first place. You can get it via `call` property.
This is not architecturally needed to have a call there, but it's much more convenient to have it right there. 
If you need pipelines somewhere outside of ktor, you need to remove `call` property from `PipelineContext`

When executing, pipeline context also carries a _subject_, some object that is being "processed". For a pipeline
without a subject it is Unit, like in `ApplicationCallPipeline`, but in a pipeline for responding it is the actual object
to be responded. It's accessible with the `subject` property. It can be changed with `proceedWith(newSubject)` – pipeline
will continue with the new subject and then get back to the caller.  

Pipelines of the same type can be merged. This is done with `merge` function on a receiving pipeline. 
All interceptors from the merging pipeline are added to receiving pipeline according to their phases.  
Pipelines are being merged when there are different points where interceptors can be installed. One example is response
pipelines that can be intercepted on the application level, on call level or per route; before we execute a response
pipeline we merge them all.   



See `PipelineTest` for examples.

