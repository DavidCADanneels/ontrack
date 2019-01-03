package net.nemerosa.ontrack.model.labels

open class Label(
        val id: Int,
        val category: String?,
        val name: String,
        val description: String?,
        val color: String,
        val computedBy: LabelProviderDescription?
)
