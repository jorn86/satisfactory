package org.hertsig.satisfactory.app

import org.hertsig.satisfactory.parser.DocumentationParser
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.bufferedReader

class App(private val installationFolder: Path) {
    fun run() {
        val docsPath = installationFolder.resolve("CommunityResources/Docs/Docs.json")
        val reader = docsPath.bufferedReader(StandardCharsets.UTF_16)
        val (items, recipes) = reader.use {
            DocumentationParser.parse(it)
        }
        println("recipe count: " + recipes.size)
        println("item count: " + items.size)
    }
}

fun main() = App(Paths.get("C:/Program Files (x86)/Steam/steamapps/common/Satisfactory")).run()
