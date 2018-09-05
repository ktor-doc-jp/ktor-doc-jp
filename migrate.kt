#!/usr/bin/env kscript

/**
 * This is a script I use as a sandbox to perform several migration tasks
 * that are tedious and error-prone to do by hand.
 *
 *  * https://github.com/holgerbrandl/kscript
 *
 *  * kscript --idea migrate.kt
 *
 */

@file:MavenRepository("ktor", "https://kotlin.bintray.com/ktor")
@file:DependsOn("io.ktor:ktor-server-netty:0.9.4")
@file:DependsOn("io.ktor:ktor-client-core:0.9.4")
@file:DependsOn("io.ktor:ktor-client-apache:0.9.4")
@file:DependsOn("io.ktor:ktor-websockets:0.9.4")
@file:DependsOn("com.fasterxml.jackson.core:jackson-databind:2.9.4")
@file:DependsOn("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.0")

@file:EntryPoint("Migrate")

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import kotlinx.coroutines.experimental.*
import java.io.*

object Migrate {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            MigrateFeatures.migrateFeatures()
        }
    }
}

object MigrateFeatures {
    val base = File("${System.getenv("HOME")}/projects/ktorio.github.io")
    val yaml = ObjectMapper(YAMLFactory().apply {
        this.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        this.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
    })

    suspend fun migrateFeatures() {
        migrateFolder(base["features"]!!, base["servers/features"]!!)
    }

    suspend fun migrateFolder(fromFolder: File, toFolder: File) {
        if (!fromFolder.exists()) error("$fromFolder do not exists")

        val fromRelative = fromFolder.relativeTo(base)
        val toRelative = toFolder.relativeTo(base)

        println("Processing... $fromRelative -> $toRelative")

        for (baseName in fromFolder.list()) {
            val relativePath = "/$fromRelative/$baseName"
            val defaultOldPermalink = relativePath.replace(".md", ".html")
            val file = fromFolder[baseName]

            if (file.isDirectory) {
                migrateFolder(fromFolder[baseName], toFolder[baseName])
                continue
            }

            if (!file.exists()) {
                continue
            }

            if (!baseName.endsWith(".md") && !baseName.endsWith(".html")) {
                println("  * $relativePath")
                fromFolder[baseName].copyTo(toFolder[baseName], overwrite = true)
                continue
            }

            println("  - $relativePath")

            //println("----------")
            //println(defaultPermalink)
            val lines = file!!.readLines()
            val info = parseFrontMatter(lines)

            // Only do stuff it has frontmatter
            if (info != null) {
                val oldPermalink = info["permalink"]?.toString() ?: defaultOldPermalink
                val permalinkBasename = oldPermalink.substringAfterLast('/')
                val newPermalink = "/$toRelative/$permalinkBasename"
                val newCategory = toRelative.path.substringBefore('/')

                info["category"] = newCategory
                if (newPermalink != oldPermalink) {
                    info["redirect_from"] = (((info["redirect_from"] as? List<String>?) ?: listOf()) + listOf(
                        oldPermalink
                    )).distinct()
                }
                if (info["permalink"] != null) {
                    info["permalink"] = newPermalink
                }
                if (info["children"] != null) {
                    info["children"] = "/$toRelative/$permalinkBasename".replace(".html", "") + "/"
                }

                val newLines = replaceFrontMatter(lines, info)
                val newContent = newLines.joinToString("\n")

                toFolder[baseName].ensureFolders().writeText(newContent)
                //println(newContent)
                //file.writeText(newContent)
            }
        }
    }

    fun replaceFrontMatter(lines: List<String>, info: Any): List<String> =
        listOf("---") + yaml.writeValueAsString(info).trimEnd().lines() + listOf("---") + stripFrontMatter(lines)

    fun stripFrontMatter(lines: List<String>): List<String> {
        if (lines.firstOrNull() == "---") {
            for (n in 1 until lines.size) {
                if (lines[n] == "---") {
                    return lines.drop(n + 1)
                }
            }
        }
        return lines
    }

    fun parseFrontMatter(lines: List<String>): LinkedHashMap<String, Any?>? {
        if (lines.firstOrNull() == "---") {
            for (n in 1 until lines.size) {
                if (lines[n] == "---") {
                    val frontMatterContent = lines.subList(1, n).joinToString("\n")
                    val value = yaml.readValue(frontMatterContent, LinkedHashMap::class.java)
                    return value as LinkedHashMap<String, Any?>
                }
            }
        }
        return null
    }
}

operator fun File.get(name: String): File = when {
    !name.contains('/') -> File(this, name)
    else -> File(this, name.substringBefore('/'))[name.substringAfter('/', "")]
}

fun File.ensureFolders(): File = this.apply {
    this.parentFile.mkdirs()
}