## Hosting Applications

Ktor applications can be self-hosted or hosted in an Application Server. This section shows to how host Ktor applications externally.

### Hosting an application in an external host

When you need to host a Ktor application in an independently maintained host (for instance Tomcat), you will need an `application.conf` file
to tell Ktor how to start your application. 

#### Defining the configuration

In the resources folder create a file named `application.conf` with the following contents
```
ktor {
    deployment {
        environment = development
        port = 8080
    }

    application {
        modules = [ my.company.MyApplication.ApplicationKt.main ]
    }
}
```

Replace `my.company.MyApplication` with your application's package, and `ApplicationKt` with then name of the
file your `Application.main` function is contained in.

#### Deploying the hosted application

// TODO 

#### Running the application from inside the IDE

Running hosted applications in a development environment such as IntelliJ IDEA, is supported by using development hosts. 

##### IntelliJ IDEA 

1. Create a new Run Configuration using "Application" as template

2. For the main class use one of the following hosts

  * Netty: use `org.jetbrains.ktor.netty.DevelopmentHost` 
  * Jetty: use `org.jetbrains.ktor.jetty.DevelopmentHost` 

    
![Main Class](resources/run-configuration-development-host.png)    
    
3. Specify the Module to be used
 
4. Save the Configuration by giving it a name

Once the configuration is saved, you can now run your application for development/debug purposes from inside IntelliJ IDEA, without having to deploy to a container or setup 
any application servers.


See also: [Configuration](Configuration)

#### Use automatic reloading

Ktor can automatically reload application when changes to class files are detected, i.e. when you build the Application.
Enable this feature by adding `autoreload` configuration to `application.conf`:

```conf
ktor {
    deployment {
        environment = development
        port = 8080
        autoreload = false
        watch = [ my.company ]
    }

    …
}
```

See also: [Automatic Reload](Automatic-Reload)
