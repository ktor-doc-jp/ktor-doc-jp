#!/usr/bin/env kscript

/**
 * This script is a small spider visiting all the local links
 * in the jekyll site looking for Not Found errors
 * and undefined anchors.
 *
 * It is written in kotlin and uses Ktor's HttpClient.
 *
 * It uses kscript:
 *
 *     https://github.com/holgerbrandl/kscript
 *
 * You can edit this file with autocompletion executing:
 *
 *     kscript --idea checkLinks.kts
 */

@file:MavenRepository("ktor", "https://kotlin.bintray.com/ktor")
@file:DependsOn("io.ktor:ktor-server-netty:0.9.2-alpha-5")
@file:DependsOn("io.ktor:ktor-client-core:0.9.2-alpha-5")
@file:DependsOn("io.ktor:ktor-client-apache:0.9.2-alpha-5")
@file:DependsOn("io.ktor:ktor-websockets:0.9.2-alpha-5")

@file:EntryPoint("CheckLinks")

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import kotlinx.coroutines.experimental.*

object CheckLinks {
	@JvmStatic
	fun main(args: Array<String>) {
		runBlocking {
			val entry = args.getOrElse(0) { "http://127.0.0.1:4000" }
			println("Loading $entry...")
			enqueue(entry, "")
			while (queue.isNotEmpty()) {
				val task = queue.remove()
				check(task)
			}
			println("Done. Visited ${visited.size} links")
		}
	}

	data class Task(val base: String, val full: String)

	val client by lazy { HttpClient(Apache) }
	val ids = hashMapOf<String, Set<String>>()
	val visited = hashSetOf<String>()
	val queue = java.util.LinkedList<Task>()

	val ID_REGEX = Regex("id=['\"](.*?)['\"]")
	val HREF_REGEX = Regex("href=['\"](.*?)['\"]")

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
				println("\u001b[31mError loading $url linked in ${task.base} : ${response.status}\u001B[0m")
			}
		}

		if (hash.isNotEmpty() && (hash !in ids[url]!!)) {
			println("Can't find #$hash in $url linked in ${task.base}")
		}
	}
}
