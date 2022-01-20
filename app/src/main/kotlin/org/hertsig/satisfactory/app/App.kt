package org.hertsig.satisfactory.app

import org.hertsig.satisfactory.RecipeChainCalculator
import org.hertsig.satisfactory.data.Building
import org.hertsig.satisfactory.data.Item
import org.hertsig.satisfactory.data.RecipeData
import org.hertsig.satisfactory.parser.DocumentationParser
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.bufferedReader

class App(private val installationFolder: Path) {
    private val rawResourceNames = listOf("Iron Ore", "Copper Ore", "Limestone", "Coal", "Caterium Ore", "Raw Quartz", "Water", "Crude Oil", "Nitrogen Gas")

    fun run() {
        val docsPath = installationFolder.resolve("CommunityResources/Docs/Docs.json")
        val reader = docsPath.bufferedReader(StandardCharsets.UTF_16)
        val (items, recipes) = reader.use {
            DocumentationParser.parse(it)
        }
        println("recipe count: " + recipes.size)
        println("item count: " + items.size)

        val rawResources = rawResourceNames.map { name -> items.single { it.name == name } }
        val screw = items.single { it.name == "Screw" }
        println(RecipeChainCalculator(rawResources, recipes)
            .calculateChains(screw))
    }
}

fun main() = App(Paths.get("C:/Program Files (x86)/Steam/steamapps/common/Satisfactory")).run()
