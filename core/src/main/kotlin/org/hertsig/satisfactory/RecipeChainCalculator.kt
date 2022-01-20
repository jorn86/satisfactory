package org.hertsig.satisfactory

import com.google.common.graph.*
import org.hertsig.satisfactory.data.Building
import org.hertsig.satisfactory.data.Item
import org.hertsig.satisfactory.data.Recipe
import org.hertsig.satisfactory.data.containsItem

@Suppress("UnstableApiUsage")
class RecipeChainCalculator(private val rawResources: List<Item>, private val recipes: List<Recipe>) {
    fun calculateChains(item: Item): Graph<Recipe> {
        val graph = GraphBuilder.directed()
            .allowsSelfLoops(true)
            .build<Recipe>()
        addRecipesProducing(item, graph)
        return graph
    }

    private fun addRecipesProducing(item: Item, graph: MutableGraph<Recipe>, to: Recipe? = null) {
        if (rawResources.contains(item) || to?.building == Building.PACKAGER) return
        recipes.filter { it.outputs.containsItem(item) }.forEach { recipe ->
            if (recipe.building != Building.PACKAGER && graph.addNode(recipe)) {
                if (to != null) graph.putEdge(recipe, to)
                println("Added ${recipe.name}")
                recipe.inputs.forEach { addRecipesProducing(it.item, graph, recipe) }
            }
        }
    }
}
