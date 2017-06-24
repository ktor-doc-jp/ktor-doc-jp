During development it is important to have fast feedback loop cycle. 
Often, restarting server would take quite some time, so Ktor provides basic auto-reload facility that
reloads just an Application. To enable this feature, add `autoreload` and `watch` keys to `ktor.deployment` 
configuration. 

`autoreload` – Feature is enabled when this value is `true`
`watch` - Array of module names that should be automatically reloaded.

```
ktor {
    deployment {
        port = 8080
        autoreload = true
        watch = [ module1, module2 ]
    }
    
    …
}
```

For now watch keys are just strings that are matched with `contains` against classpath entries of the loaded 
application, such as a jar name or a project directory name. 
These classes are then loaded with special `ClassLoader` that is recycled when change is detected.

_Note:_ `ktor-core` classes are specifically excluded from auto-reloading, so if you are working on something in ktor itself, 
don't expect it to autoreload. It can't work because core classes are loaded before auto-reload machinery kicks in. 
The exclusion can potentially be smaller, but it's hard to analyse all the transitive closure of types loaded during
startup.
