package org.hertsig.satisfactory.data

import java.time.Duration

data class Recipe(
    val name: String,
    val building: Building,
    override val inputs: List<RecipeData>,
    override val outputs: List<RecipeData>,
    val alternate: Boolean,
    val duration: Duration,
) : ManufacturingPlan {
    override fun toString() = (if (alternate) "$name (Alternate)" else name) + ": ${building.displayName}"
}

fun List<RecipeData>.containsItem(item: Item) = any { it.item == item }
