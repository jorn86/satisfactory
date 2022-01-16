package org.hertsig.satisfactory.parser

data class RawRecipe(
    var className: String? = null,
    var fullName: String? = null,
    var blueprintClass: String? = null,
    var displayName: String? = null,
    var ingredients: String? = null,
    var product: String? = null,
    var duration: Long? = null,
    var producedIn: String? = null,
    var variablePowerConsumptionConstant: Double? = null,
    var variablePowerConsumptionFactor: Double? = null,
)
