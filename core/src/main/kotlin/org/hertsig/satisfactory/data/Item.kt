package org.hertsig.satisfactory.data

data class Item(
    val name: String,
    val description: String,
    val stackSize: StackSize?,
    val resourceSinkPoints: Int,
) {
    fun isFluid() = stackSize == null
    fun canBeSunk() = resourceSinkPoints != 0
    override fun toString() = name
}
