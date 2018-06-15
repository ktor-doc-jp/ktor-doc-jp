---
title: Google OAuth
caption: How to implement an OAuth login with Google
category: quickstart
---

In this guide we are going to implement a login using OAuth. Ideally you should have already some notions of Ktor.
For example, you can make the [Website guide](/quickstart/guides/website.html)

### Creating a host entry pointing to 127.0.0.1

Google's OAuth require a redirect URLs that can't be IP addresses or localhost.
So for development purposes we will need a proper host pointing to 127.0.0.1.
That host is not required to be accessible from outside our computer, so we can force a local host.
There is a public domain <http://lvh.me/> pointing to localhost/127.0.0.1, but you might want to provide your
own host locally for security reasons.

For this, you can add an entry in [the hosts file](https://en.wikipedia.org/wiki/Hosts_(file)) of your machine.

#### MacOS/Linux

In MacOS and Linux computers, you can create hosts locally visible to you own machine by editing the `/etc/hosts` file.
Since this file is important for security, you will need root access to edit it.

```sudo nano /etc/hosts```
or
```sudo vi /etc/hosts```

The structure of this file is simple: # character for comments, and each non empty, and non comment line should contain an IP address followed by several host names separated by spaces or tabs.
You will probably have at least one entry for localhost like this one:
```
127.0.0.1       localhost
```

#### Windows

In windows, the host file is hold here `%SystemRoot%\System32\drivers\etc\hosts`. You will need admin privileges
to edit this file. For example, you can use Notepad++ opened as administrator.

You can also paste in the Windows Explorer the path `%SystemRoot%\System32\drivers\etc` and then right click
in the hosts file to edit it. The structure is the same as the MacOS/Linux.

#### Creating the entry

In this guide I'm going to associate `mydevenv` to `127.0.0.1`, but you can change it for your needs.
For example you could use `me.mydomain.com`.

```
127.0.0.1       mydevenv
```

### Google Developers Console

To be able to use OAuth with any provider, you will need a public `clientId`, and a private `clientSecret`.
In the case of Google login, you can create it using the Google's Developers Console:
<https://console.developers.google.com/>

First you have to create a new project in the developers console

Then you have to go to the `API & Services` → `Credentials` → `Create Credentials` → `OAuth Client Id` → `Web Application`.
In that form for creating the credentials, we are going to set our fake domain pointing to 127.0.0.1.

* **Authorized JavaScript origins:** http://mydevenv:8080
* **Authorized redirect URIs:** http://mydevenv:8080/login

You can change those values later, or add additional authorized urls by editing the credentials.

Press the `Create` button.

You will see a modal dialog with the following:

OAuth client
* Here is your client ID: xxxxxxxxxxx.apps.googleusercontent.com
* Here is your client secret: yyyyyyyyyyy

You will have to go to the `OAuth Consent screen` tab and configure the non-optional fields from there.

### Configuring our application

A simple embedded application would look like this:

```kotlin
val googleOauthProvider = OAuthServerSettings.OAuth2ServerSettings(
    name = "google",
    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
    accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",
    requestMethod = HttpMethod.Post,

    clientId = "xxxxxxxxxxx.apps.googleusercontent.com",
    clientSecret = "yyyyyyyyyyy",
    defaultScopes = listOf("profile") // no email, but gives full name, picture, and id
)

class MySession(val userId: String)

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080) {
        install(WebSockets)
        install(Sessions) {
            cookie<MySession>("oauthSampleSessionId")
        }
        install(Authentication) {
            oauth("google-oauth") {
                client = HttpClient(Apache)
                providerLookup = { googleOauthProvider }
                urlProvider = {
                    redirectUrl("/login")
                }
            }
        }
        routing {
            get("/") {
                val session = call.sessions.get<MySession>()
                call.respondText("HI ${session?.userId}")
            }
            authenticate("google-oauth") {
                route("/login") {
                    handle {
                        val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                            ?: error("No principal")

                        val cc = HttpClient(Apache).get<String>("https://www.googleapis.com/userinfo/v2/me") {
                            header("Authorization", "Bearer ${principal.accessToken}")
                        }

                        val ccs = ObjectMapper().readValue<Map<String, Any?>>(cc)
                        val id = ccs["id"] as String?

                        if (id != null) {
                            call.sessions.set(MySession(id))
                        }
                        call.respondRedirect("/")
                    }
                }
            }
        }
    }.start(wait = true)
}

private fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") 80 else 443
    val hostPort = request.host()!! + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}
``` 

### Additional resources

* Google oauth playground: <https://developers.google.com/oauthplayground/>
