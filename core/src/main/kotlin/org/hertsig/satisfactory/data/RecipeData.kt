package org.hertsig.satisfactory.data

data class RecipeData(
    val amount: Int,
    val item: Item,
) {
    fun display() = "$amount ${item.name}"
}
