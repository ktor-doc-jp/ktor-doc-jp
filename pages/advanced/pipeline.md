---
title: Pipeline
category: advanced
permalink: /advanced/pipeline.html
caption: Internal Pipeline Machinery
---

The pipeline is a sequence of blocks(lambdas) that are called one after another, with the ability to extend the list
and call remaining pipeline and then get back to current function. All blocks are suspending functions/lambdas, thus
the whole pipeline is async

```
[block1] -> [block2] -> [block3]
```

Block can run in one of few ways:

* run to the end. In this case, the next block is called, or the pipeline finishes if it was last block
* throw exception. exception propagates back, and the pipeline is canceled
* call `proceed(…)` function. The rest of the pipeline is executed and then the code after `proceed` is executed
* call `finish()` function. The pipeline finishes without any exceptions and without executing the rest of the pipeline

The pipeline is filled with blocks using `intercept` function.
The pipeline is executed using `execute` function.

Order of blocks is determined by the order of phases they are installed into, then by installation order.
Phases are defined when the pipeline is created, and can be augmented to add more phases using `pipeline.phases`

The pipeline is bound to an `ApplicationCall` that initiated its execution in the first place. You can get it via `call` property.
This is not architecturally needed to have a call there, but it's much more convenient to have it right there. 
If you need pipelines somewhere outside of ktor, you need to remove the `call` property from `PipelineContext`

When executing, the pipeline context also carries a _subject_, some object that is being "processed". For a pipeline
without a subject it is Unit, like in `ApplicationCallPipeline`, but in a pipeline for responding it is the actual object
to be responded. It's accessible with the `subject` property. It can be changed with `proceedWith(newSubject)` – the pipeline
will continue with the new subject and then get back to the caller.  

Pipelines of the same type can be merged. This is done with `merge` function on a receiving a pipeline. 
All interceptors from merging the pipeline are added to the receiving pipeline according to their phases.  
Pipelines are being merged when there are different points where interceptors can be installed. One example is the response
pipelines that can be intercepted on the application level, on call level, or per route; before we execute a response
pipeline we merge them all.   



See `PipelineTest` for examples.

