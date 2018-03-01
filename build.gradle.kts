import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import kotlinx.coroutines.experimental.*
import org.apache.xerces.util.*

buildscript {
    val kotlin_version = "1.2.21"
    val ktor_version = "0.9.2-alpha-1"

    repositories {
        jcenter()
        maven(url = "http://kotlin.bintray.com/ktor")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
        classpath("io.ktor:ktor-server-netty:$ktor_version")
        classpath("io.ktor:ktor-client-core:$ktor_version")
        classpath("io.ktor:ktor-client-apache:$ktor_version")
        classpath("io.ktor:ktor-websockets:$ktor_version")
    }
}

//> Internal compiler error: Back-end (JVM) Internal error: Don't know how to generate outer expression for class CheckLinks
//fun HttpStatusCode.isOk() = this.value < 400

object CheckLinks {
    data class Task(val base: String, val full: String)

    val client by lazy { HttpClient(Apache) }
    val ids = hashMapOf<String, Set<String>>()
    val visited = hashSetOf<String>()
    val queue = java.util.LinkedList<Task>()

    val ID_REGEX = Regex("id=['\"](.*?)['\"]")
    val HREF_REGEX = Regex("href=['\"](.*?)['\"]")

    // Using logger.info here:
    //> Internal compiler error: Back-end (JVM) Internal error: Don't know how to generate outer expression for class CheckLinks

    fun enqueue(base: String, link: String) {
        //println("Processing: '$base' -> '$link'...")
        try {
            val full: String = java.net.URI(base).resolve(link).toURL().toString()
            if (full in visited) return
            visited += full
            queue.add(Task(base, full))
        } catch (e: Throwable) {
            // Ignore URL
        }
    }

    suspend fun check(task: Task) {
        //println("Checking ${task.full}...")
        val full = task.full
        val (url, hash) = full.split("#", limit = 2) + listOf("")

        if (!url.startsWith("http://127.0.0.1:4000")) {
            //println("Ignoring external link $url...")
            return
        }

        if (url !in ids) {
            val response = client.get<HttpResponse>(url)
            //println("Downloaded: $url")
            //if (response.status.isOk()) {
            if (response.status.value < 400) {
                val html = response.readText()
                val idList = ID_REGEX.findAll(html).map { it.groupValues[1] }
                ids[url] = idList.toSet()
                val links = HREF_REGEX.findAll(html).map { it.groupValues[1] }
                for (link in links) {
                    enqueue(task.full, link)
                }
            } else {
                ids[url] = setOf()
                println("Error loading $url linked in ${task.base} : ${response.status}")
            }
        }

        if (hash.isNotEmpty() && (hash !in ids[url]!!)) {
            println("Can't find #$hash in $url linked in ${task.base}")
        }
    }

    suspend fun run() {
        enqueue("http://127.0.0.1:4000", "")
        while (queue.isNotEmpty()) {
            val task = queue.remove()
            check(task)
        }
    }
}

tasks {
    "checkLinks" {
        outputs.upToDateWhen { false }

        doLast {
            runBlocking {
                CheckLinks.run()
            }
        }
    }
}


