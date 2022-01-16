package org.hertsig.satisfactory.data

sealed interface ManufacturingPlan {
    val inputs: List<RecipeData>
    val outputs: List<RecipeData>
}
