---
title: Deploy
category: servers
permalink: /servers/deploy.html
keywords: google-appengine-standard jetty-embedded jetty-war netty tomcat-war heroku nginx war fat-jar docker packing proguard
caption: Deployment 
priority: 300
---

{::options toc_levels="1..3" /}

Once you are ready with your application, you will probably want to put it somewhere.

In this page, you will learn how to deploy your application to several providers and containers. 

**Table of contents:**

* TOC
{:toc}

## Packing

When deploying, normally you will want to generate a single archive with all your
classes, dependencies, and resources packed together: either in a single JAR archive
(also called Fat JAR) or a WAR file (Web Application Resource).

### Fat JAR (Standalone)
{: #fat-jar}

A *fat-jar* (or *uber-jar*) archive allows you to generate a single archive to run your standalone
embedded application directly using Java: `java -jar yourapplication.jar`.

This is the preferred way for running it in a container like [docker](#docker), when deploying to [heroku](#heroku)
or when being reverse-proxied with [nginx](#nginx). 

#### Gradle
{: #fat-jar-gradle}

When using Gradle, you can use the `shadow` gradle plugin to generate it. For example,
to generate a fat JAR using netty as an engine:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:2.0.2'
    }
}

apply plugin: 'com.github.johnrengelman.plugin-shadow'
apply plugin: 'kotlin'
apply plugin: 'application'

mainClassName = 'io.ktor.server.netty.DevelopmentEngine'
```
{: .compact }

#### Maven
{: #fat-jar-maven}

When using Maven, you can generate a fat JAR archive with the `maven-assembly-plugin`. For example, to generate
a fat JAR using netty as an engine:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.1.0</version>
    <configuration>
        <descriptorRefs>
            <descriptorRef>jar-with-dependencies</descriptorRef>
        </descriptorRefs>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>io.ktor.server.netty.DevelopmentEngine</mainClass>
            </manifest>
        </archive>
    </configuration>
    <executions>
        <execution>
            <id>assemble-all</id>
            <phase>package</phase>
            <goals>
                <goal>single</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
{: .compact }

### WAR (Servlet Container)
{: #war}

A WAR archive allows you to easily deploy your application inside your web container / servlet container,
by just copying it to its `webapps` folder. Ktor supports two popular servlet containers: [Jetty](#jetty) and [Tomcat](#tomcat).
It also serves when deploying to [google app engine](#google-app-engine).

To generate a war file, you can use the gretty gradle plugin. You also need a `WEB-INF/web.xml` which looks like this:

`webapp/WEB-INF/web.xml`:
```xml
<?xml version="1.0" encoding="ISO-8859-1" ?>

<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">
    <!-- path to application.conf file, required -->
    <!-- note that this file is always loaded as an absolute path from the classpath -->
    <context-param>
        <param-name>io.ktor.ktor.config</param-name>
        <param-value>application.conf</param-value>
    </context-param>
	
    <servlet>
        <display-name>KtorServlet</display-name>
        <servlet-name>KtorServlet</servlet-name>
        <servlet-class>io.ktor.server.servlet.ServletApplicationEngine</servlet-class>

        <!-- required! -->
        <async-supported>true</async-supported>

        <!-- 100mb max file upload, optional -->
        <multipart-config>
            <max-file-size>304857600</max-file-size>
            <max-request-size>304857600</max-request-size>
            <file-size-threshold>0</file-size-threshold>
        </multipart-config>
    </servlet>

    <servlet-mapping>
        <servlet-name>KtorServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

</web-app>
```
{: .compact}

`build.gradle`:
```groovy
buildscript {
    ext.gretty_version = '2.0.0'

    repositories {
        jcenter()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "org.akhikhl.gretty:gretty:$gretty_version"
    }
}

apply plugin: 'kotlin'
apply plugin: 'war'
apply plugin: 'org.akhikhl.gretty'

webAppDirName = 'webapp'

gretty {
    contextPath = '/'
    logbackConfigFile = 'resources/logback.xml'
}

sourceSets {
    main.kotlin.srcDirs = [ 'src' ]
    main.resources.srcDirs = [ 'resources' ]
}

repositories {
    jcenter()
    maven { url "http://kotlin.bintray.com/ktor" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-servlet:$ktor_version"
    compile "io.ktor:ktor-html-builder:$ktor_version"
    compile "ch.qos.logback:logback-classic:$logback_version"
}

kotlin.experimental.coroutines = 'enable'

task run

afterEvaluate {
    run.dependsOn(tasks.findByName("appRun"))
}
```
{: .compact}

This gradle buildscript defines [several tasks](http://akhikhl.github.io/gretty-doc/Gretty-tasks) that
you can use to run your application.

In the case where you only need to generate a war file, there is a `war` task defined in the war plugin.<br />
Just run `./gradlew war` and it will generate a `/build/libs/projectname.war` file.
{: .note #generate-war-file }

For a full example: <https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty-war>
{: .note.example}

## Proguard
{: #proguard}

If you have some restrictions on your JAR size (for example when deploying a free application to [heroku](#heroku)),
you can use proguard to shrink it. If you are using gradle, it is pretty straightforward to use the
`proguard-gradle` plugin. You only have to remember to keep: your main module method, the DevelopmentEngine
class, and the Kotlin reflect classes. You can then fine-tune it as required:

```groovy
buildscript {
    ext.proguard_version = '6.0.1'
    dependencies {
        classpath "net.sf.proguard:proguard-gradle:$proguard_version"
    }
}

task minimizedJar(type: proguard.gradle.ProGuardTask, dependsOn: shadowJar) {
    injars "build/libs/my-application.jar"
    outjars "build/libs/my-application.min.jar"
    libraryjars System.properties.'java.home' + "/lib/rt.jar"
    printmapping "build/libs/my-application.map"
    ignorewarnings
    dontobfuscate
    dontoptimize
    dontwarn

    def keepClasses = [
            'io.ktor.server.netty.DevelopmentEngine', // The DevelopmentEngine you use, netty in this case.
            'kotlin.reflect.jvm.internal.**',
            'io.ktor.samples.hello.HelloApplicationKt', // The class containing your module defined in the application.conf
            'kotlin.text.RegexOption'
    ]

    for (keepClass in keepClasses) {
        keep access: 'public', name: keepClass, {
            method access: 'public'
            method access: 'private'
        }
    }
}
```
{: .compact }

You have a full example on: <https://github.com/ktorio/ktor-samples/tree/master/other/proguard> 
{: .note.example}

## Containers
{: #containers}

### Docker
{: #docker}

Docker is a container engine: it allows you to pack and run applications, in a sandboxed layered
lightweight environment, with its own isolated filesystem, operating system, and resources.

You usually have to create a `Dockerfile` for monolithic services, and a `docker-compose.yml` 
when your container needs to interact with other services, like for example a database or a redis. 

First you have to create a [fat-jar file](#fat-jar) with your application. And a `Dockerfile`, which looks like this:
```
FROM openjdk:8-jre-alpine
COPY ./build/libs/my-application.jar /root/my-application.jar
WORKDIR /root
CMD ["java", "-server", "-Xms4g", "-Xmx4g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "my-application.jar"]
```

For deploying to Docker simply you can check out the [docker quickstart](/quickstart/quickstart/docker.html) page for full details.

#### Nginx
{: #nginx}

When using Docker with multiple domains, you might want to use the 
[nginx-proxy](https://github.com/jwilder/nginx-proxy) image and the 
[letsencrypt-nginx-proxy-companion](https://github.com/JrCs/docker-letsencrypt-nginx-proxy-companion) image
to serve multiple domains/subdomains in a single machine/ip and to automatically provide HTTPS,
using let's encrypt.

After configuring the nginx-proxy and letsencrypt-nginx-proxy-companion, your docker-compose.yml file
(without additional services) might look like this:

```yaml
version: '2'
services:
  web:
    build:
      context: ./
      dockerfile: Dockerfile
    expose:
      - 8080
    environment:
      - VIRTUAL_HOST=mydomain.com
      - VIRTUAL_PORT=8080
      - LETSENCRYPT_HOST=mydomain.com
      - LETSENCRYPT_EMAIL=myemail@mydomain.com
    networks:
      - reverse-proxy
    restart: always

networks:
  backend:
  reverse-proxy:
    external:
      name: reverse-proxy
```
{: .compact }

You can start it with `docker-compose up -d` and it will be restarted if the service fails or
after a system reboot.

If the DNS for the specified domain is pointing to your server and you have configured the nginx-proxy and its companion correctly,
the letsencrypt companion will contact with letsencrypt and will grab and configure the certificate automatically
for you. So you will be able to access your http-only service via: https://mydomain.com/ nginx will handle the SSL certificates
and will contact your server via plain HTTP.

### Tomcat
{: #tomcat}

You have to generate a [war file](#war) and put it in the Tomcat `webapps` folder.

For a complete example, check:
<https://github.com/ktorio/ktor-samples/tree/master/deployment/tomcat-war>
{: .note.example}

### Jetty
{: #jetty}

You have to generate a [war file](#war) and put it in the Jetty `webapps` folder.

For a complete example, check:
<https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty-war>
{: .note.example}

## Hosting 
{: #hosting}

### Heroku
{: #heroku}

There is a quickstart repository for Heroku: <https://github.com/orangy/ktor-heroku-start>
{: .note.example}

#### Preparing

For using Heroku, you will need Java, Maven/Gradle and the [Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli)

You will also need to configure your public key in the Heroku configuration.

You can try the `heroku --version` command to see if you have the command line installed:

```
> heroku --version
heroku-cli/6.15.36 (darwin-x64) node-v9.9.0
```

You will also need an `app.json` file describing your projects and your dependencies:
```json
{
  "name": "Start on Heroku: Kotlin",
  "description": "A barebones Kotlin app, which can easily be deployed to Heroku.",
  "image": "heroku/java",
  "addons": [ "heroku-postgresql" ]
}
```

You will also need a `Procfile` describing what to execute:
```
web:    java -jar target/helloworld.jar
```

And a `system.properties` file describing your java version:

```
java.runtime.version=1.8
```

#### Running locally

And a file called `.env` along with the other files(required for development).
This will contain environment variables that Heroku will pass to the application.
For example, for the quickstart:

```properties
PORT=8080
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/java_database_name
```

If your local installation of postgresql has a user/password, you have to change the jdbc url too:
```properties
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/java_database_name?user=user&password=password
```

You will also first need to create the database:

```
> psql -c "CREATE DATABASE java_database_name;"

CREATE DATABASE
```

With these files, you can use Gradle or Maven to create a [fat-jar](#fat-jar) and adjust the `Procfile`
to point to the right file.

After building the jar, in Unix systems you can use `heroku local:start` to start your server.

#### Deploying

You first have to create an app or set the git remote. `heroku create` will create an app
with a random available name and it will set a git remote of the repo.
After calling `heroku create`, you should see something like this:

```
> heroku create
Creating app... done, ⬢ demo-demo-12345
https://demo-demo-12345.herokuapp.com/ | https://git.heroku.com/demo-demo-12345.git
```

This effectively adds a `heroku` remote to your git clone:

```
> cat .git/config
...
[remote "heroku"]
	url = https://git.heroku.com/demo-demo-12345.git
	fetch = +refs/heads/*:refs/remotes/heroku/*
```

After that, you have to push your git changes to the `heroku` remote. And it does a build on push:

```
> git push heroku master
Counting objects: 90, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (59/59), done.
Writing objects: 100% (90/90), 183.08 KiB | 5.55 MiB/s, done.
Total 90 (delta 21), reused 0 (delta 0)
remote: Compressing source files... done.
remote: Building source:
remote:
remote: -----> Java app detected
remote: -----> Installing JDK 1.8... done
remote: -----> Executing: ./mvnw -DskipTests clean dependency:list install
...
remote:        [INFO] BUILD SUCCESS
remote:        [INFO] ------------------------------------------------------------------------
remote:        [INFO] Total time: 49.698 s
remote:        [INFO] Finished at: 2018-03-23T04:33:01+00:00
remote:        [INFO] Final Memory: 41M/399M
remote:        [INFO] ------------------------------------------------------------------------
remote: -----> Discovering process types
remote:        Procfile declares types -> web
remote:
remote: -----> Compressing...
remote:        Done: 60.7M
remote: -----> Launching...
remote:        Released v4
remote:        https://demo-demo-12345.herokuapp.com/ deployed to Heroku
remote:
remote: Verifying deploy... done.
To https://git.heroku.com/demo-demo-12345.git
 * [new branch]      master -> master
```
{: .compact }

Now you can execute `heroku open` to open your application in your browser:

```
heroku open
```

In this case, it will open: https://demo-demo-12345.herokuapp.com/

Remember that Heroku sets an environment variable called `PORT` which you have to bind to instead of
a fixed port.<br/>
When using embeddedServer you will have to use `System.getenv`, while when using `application.conf` you will
have to set `ktor.deployment.port = ${PORT}`.<br/>
Check out the page about
[using environment variables in the configuration](http://127.0.0.1:4000/servers/configuration.html#environment-variables)
for more information.
{: .note}

### Google App Engine
{: #google-app-engine}

You can check out a full google appengine sample, here:
<https://github.com/ktorio/ktor-samples/tree/master/deployment/google-appengine-standard>
{: .note.example}

#### Preparing

You first need to install the `gcloud` cli. You can grab it from here:
<https://cloud.google.com/sdk/docs/> and follow the described steps to install it.

For example, a macOS setup might look something like this:

```
> wget https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-194.0.0-darwin-x86_64.tar.gz
> tar -xzf google-cloud-sdk-194.0.0-darwin-x86_64.tar.gz
> cd google-cloud-sdk

> ./install.sh
Welcome to the Google Cloud SDK!

To help improve the quality of this product, we collect anonymized usage data
and anonymized stacktraces when crashes are encountered; additional information
is available at <https://cloud.google.com/sdk/usage-statistics>. You may choose
to opt out of this collection now (by choosing 'N' at the below prompt), or at
any time in the future by running the following command:

    gcloud config set disable_usage_reporting true

Do you want to help improve the Google Cloud SDK (Y/n)?  n


Your current Cloud SDK version is: 194.0.0
The latest available version is: 194.0.0

┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                  Components                                                 │
├───────────────┬──────────────────────────────────────────────────────┬──────────────────────────┬───────────┤
│     Status    │                         Name                         │            ID            │    Size   │
├───────────────┼──────────────────────────────────────────────────────┼──────────────────────────┼───────────┤
│ Not Installed │ App Engine Go Extensions                             │ app-engine-go            │ 151.3 MiB │
│ Not Installed │ Cloud Bigtable Command Line Tool                     │ cbt                      │   4.0 MiB │
│ Not Installed │ Cloud Bigtable Emulator                              │ bigtable                 │   3.8 MiB │
│ Not Installed │ Cloud Datalab Command Line Tool                      │ datalab                  │   < 1 MiB │
│ Not Installed │ Cloud Datastore Emulator                             │ cloud-datastore-emulator │  17.9 MiB │
│ Not Installed │ Cloud Datastore Emulator (Legacy)                    │ gcd-emulator             │  38.1 MiB │
│ Not Installed │ Cloud Pub/Sub Emulator                               │ pubsub-emulator          │  33.4 MiB │
│ Not Installed │ Emulator Reverse Proxy                               │ emulator-reverse-proxy   │  14.5 MiB │
│ Not Installed │ Google Container Local Builder                       │ container-builder-local  │   3.7 MiB │
│ Not Installed │ Google Container Registry's Docker credential helper │ docker-credential-gcr    │   2.5 MiB │
│ Not Installed │ gcloud Alpha Commands                                │ alpha                    │   < 1 MiB │
│ Not Installed │ gcloud Beta Commands                                 │ beta                     │   < 1 MiB │
│ Not Installed │ gcloud app Java Extensions                           │ app-engine-java          │ 118.9 MiB │
│ Not Installed │ gcloud app PHP Extensions                            │ app-engine-php           │  21.9 MiB │
│ Not Installed │ gcloud app Python Extensions                         │ app-engine-python        │   6.2 MiB │
│ Not Installed │ gcloud app Python Extensions (Extra Libraries)       │ app-engine-python-extras │  27.8 MiB │
│ Not Installed │ kubectl                                              │ kubectl                  │  12.2 MiB │
│ Installed     │ BigQuery Command Line Tool                           │ bq                       │   < 1 MiB │
│ Installed     │ Cloud SDK Core Libraries                             │ core                     │   7.4 MiB │
│ Installed     │ Cloud Storage Command Line Tool                      │ gsutil                   │   3.4 MiB │
└───────────────┴──────────────────────────────────────────────────────┴──────────────────────────┴───────────┘
To install or remove components at your current SDK version [194.0.0], run:
  $ gcloud components install COMPONENT_ID
  $ gcloud components remove COMPONENT_ID

To update your SDK installation to the latest version [194.0.0], run:
  $ gcloud components update


Modify profile to update your $PATH and enable shell command
completion?

Do you want to continue (Y/n)?  Y

The Google Cloud SDK installer will now prompt you to update an rc
file to bring the Google Cloud CLIs into your environment.

Enter a path to an rc file to update, or leave blank to use
[/Users/user/.zshrc]:
Backing up [/Users/user/.zshrc] to [/Users/user/.zshrc.backup].
[/Users/user/.zshrc] has been updated.

==> Start a new shell for the changes to take effect.


For more information on how to get started, please visit:
  https://cloud.google.com/sdk/docs/quickstarts
```
{: .compact}

After that, you can start a new shell, and you should have access to the `gcloud` cli. For example:

```
> gcloud --version
Google Cloud SDK 194.0.0
bq 2.0.30
core 2018.03.16
gsutil 4.29
```

You will also need to install some components with the cli (`gcloud components install app-engine-java`):

```
> gcloud components install app-engine-java


Your current Cloud SDK version is: 194.0.0
Installing components from version: 194.0.0

┌────────────────────────────────────────────────────┐
│        These components will be installed.         │
├──────────────────────────────┬─────────┬───────────┤
│             Name             │ Version │    Size   │
├──────────────────────────────┼─────────┼───────────┤
│ gRPC python library          │         │           │
│ gRPC python library          │   1.9.1 │   7.6 MiB │
│ gcloud app Java Extensions   │  1.9.63 │ 118.9 MiB │
│ gcloud app Python Extensions │  1.9.67 │   6.2 MiB │
└──────────────────────────────┴─────────┴───────────┘

For the latest full release notes, please visit:
  https://cloud.google.com/sdk/release_notes

Do you want to continue (Y/n)?  Y

╔════════════════════════════════════════════════════════════╗
╠═ Creating update staging area                             ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Installing: gRPC python library                          ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Installing: gRPC python library                          ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Installing: gcloud app Java Extensions                   ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Installing: gcloud app Python Extensions                 ═╣
╠════════════════════════════════════════════════════════════╣
╠═ Creating a backup and activating a new installation      ═╣
╚════════════════════════════════════════════════════════════╝

Performing post-processing steps...done.

Update done!
```
{: .compact}

For your project, you can use gradle and the official `appengine-gradle-plugin`. So a `build.gradle` would look like this:

```groovy
buildscript {
    ext.appengine_version = '1.9.60'
    ext.appengine_plugin_version = '1.3.4'

    repositories {
        jcenter()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "com.google.cloud.tools:appengine-gradle-plugin:$appengine_plugin_version"
    }
}

apply plugin: 'kotlin'
apply plugin: 'war'
apply plugin: 'com.google.cloud.tools.appengine'

// appengine does not honor this property, so we are forced to use deep Maven tree layout
// webAppDirName = file('webapp')

sourceSets {
    main.kotlin.srcDirs = [ 'src/main/kotlin' ]
}

repositories {
    jcenter()
    maven { url "http://kotlin.bintray.com/ktor" }
}

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    compile "io.ktor:ktor-server-servlet:$ktor_version"
    compile "io.ktor:ktor-html-builder:$ktor_version"
    compile "org.slf4j:slf4j-jdk14:$slf4j_version"

    providedCompile "com.google.appengine:appengine:$appengine_version"
}

kotlin.experimental.coroutines = 'enable'

task run(dependsOn: appengineRun)
```
{: .compact}

Once everything is configured, you can now run the application locally, using the gradle task `appengineRun`:

In this case, these commands are executed in the root of the ktor-samples repository <https://github.com/ktorio/ktor-samples/>:  

```
./gradlew :google-appengine-standard:appengineRun
```

It should start the server in <http://localhost:8080/> and the admin in <http://localhost:8080/_ah/admin>.

#### Deploying

First, we need to create a project `gcloud projects create demo-demo-123456 --set-as-default`:

```
> gcloud projects create demo-demo-123456 --set-as-default
Create in progress for [https://cloudresourcemanager.googleapis.com/v1/projects/demo-demo-123456].
Waiting for [operations/pc.7618150612308930095] to finish...done.
Updated property [core/project] to [demo-demo-123456].
```

And then we need to create an application using `gcloud app create`:

```
> gcloud app create
You are creating an app for the project [demo-demo-123456].
WARNING: Creating an App Engine application for a project is irreversible, and the region
cannot be changed. More information about regions is at
<https://cloud.google.com/appengine/docs/locations>.

Please choose the region where you want your App Engine application
located:

 [1] europe-west2  (supports standard and flexible)
 [2] us-central    (supports standard and flexible)
 [3] europe-west   (supports standard and flexible)
 [4] europe-west3  (supports standard and flexible)
 [5] us-east1      (supports standard and flexible)
 [6] us-east4      (supports standard and flexible)
 [7] asia-northeast1 (supports standard and flexible)
 [8] asia-south1   (supports standard and flexible)
 [9] australia-southeast1 (supports standard and flexible)
 [10] southamerica-east1 (supports standard and flexible)
 [11] northamerica-northeast1 (supports standard and flexible)
 [12] cancel
Please enter your numeric choice:  1

Creating App Engine application in project [demo-demo-123456] and region [europe-west2]....done.
Success! The app is now created. Please use `gcloud app deploy` to deploy your first app.
```
{: .compact}

Now we can deploy the application using `gradle appengineDeploy`:

```
> gradle :google-appengine-standard:appengineDeploy
Starting a Gradle Daemon (subsequent builds will be faster)
Reading application configuration data...
Mar 23, 2018 6:32:09 AM com.google.apphosting.utils.config.IndexesXmlReader readConfigXml
INFORMATION: Successfully processed /Users/user/projects/ktor-samples/deployment/google-appengine-standard/build/exploded-google-appengine-standard/WEB-INF/appengine-generated/datastore-indexes-auto.xml


Beginning interaction for module default...
0% Scanning for jsp files.
0% Generated git repository information file.
Success.
Temporary staging for module default directory left in /Users/user/projects/ktor-samples/deployment/google-appengine-standard/build/staged-app
Services to deploy:

descriptor:      [/Users/user/projects/ktor-samples/deployment/google-appengine-standard/build/staged-app/app.yaml]
source:          [/Users/user/projects/ktor-samples/deployment/google-appengine-standard/build/staged-app]
target project:  [demo-demo-123456]
target service:  [default]
target version:  [20180323t063212]
target url:      [https://demo-demo-123456.appspot.com]


Beginning deployment of service [default]...
Some files were skipped. Pass `--verbosity=info` to see which ones.
You may also view the gcloud log file, found at
[/Users/user/.config/gcloud/logs/2018.03.23/06.32.10.739209.log].
#============================================================#
#= Uploading 38 files to Google Cloud Storage               =#
#============================================================#
File upload done.
Updating service [default]...
..............done.
Setting traffic split for service [default]...
.......done.
Deployed service [default] to [https://demo-demo-123456.appspot.com]

You can stream logs from the command line by running:
  $ gcloud app logs tail -s default

To view your application in the web browser run:
  $ gcloud app browse

BUILD SUCCESSFUL in 42s
6 actionable tasks: 2 executed, 4 up-to-date
```
{: .compact}

Now you can view your application in your browser with `gcloud app browse`. It will open
the application. In this case: https://demo-demo-123456.appspot.com

## Samples
{: #samples }

In the Ktor's samples repository, you can find examples and README files
on [how to deploy to specific providers](https://github.com/ktorio/ktor-samples/tree/master/deployment).

* <https://github.com/ktorio/ktor-samples/tree/master/deployment/docker>
* <https://github.com/ktorio/ktor-samples/tree/master/deployment/google-appengine-standard>
* <https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty-embedded>
* <https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty-war>
* <https://github.com/ktorio/ktor-samples/tree/master/deployment/jetty>
* <https://github.com/ktorio/ktor-samples/tree/master/deployment/netty>
* <https://github.com/ktorio/ktor-samples/tree/master/deployment/tomcat-war>
