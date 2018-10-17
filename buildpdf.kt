#!/usr/bin/env kscript

import java.net.*

// https://builds.wkhtmltopdf.org/0.12.6-dev/
fun main(args: Array<String>) {
    val pages = downloadText("http://127.0.0.1:4000/pages.txt").lines().map { it.trim() }.filter { it.isNotBlank() }

    val wargs = buildList<String> {
        add("-l")
        add("--disable-javascript")
        add("--print-media-type")
        add("toc")
        for (page in pages) {
            add(page)
        }
        add("ktor.pdf")
    }
    exec("wkhtmltopdf", *wargs.toTypedArray())
}

fun downloadText(url: String): String {
    return URL(url).openStream().readBytes().toString(Charsets.UTF_8)
}

fun exec(process: String, vararg args: String) {
    val pb = ProcessBuilder(process, *args).inheritIO()
    val p = pb.start()
    p.waitFor()
}

fun <T> buildList(callback: ArrayList<T>.() -> Unit): List<T> {
    return arrayListOf<T>().apply(callback)
}