package org.hertsig.satisfactory.parser

data class RawItem(
    var className: String? = null,
    var displayName: String? = null,
    var description: String? = null,
    var abbreviatedDisplayName: String? = null,
    var stackSize: String? = null,
    var canBeDiscarded: Boolean? = null,
    var form: String? = null,
    var smallIcon: String? = null,
    var bigIcon: String? = null,
    var subcategories: String? = null,
    var resourceSinkPoints: Int? = null,
)
