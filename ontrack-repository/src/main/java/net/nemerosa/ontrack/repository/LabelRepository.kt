package net.nemerosa.ontrack.repository

import net.nemerosa.ontrack.model.labels.LabelForm

interface LabelRepository {

    /**
     * Creation of a new label
     */
    fun newLabel(form: LabelForm, computedBy: String? = null): LabelRecord

    /**
     * Gets list of all labels, ordered by category and name
     */
    val labels: List<LabelRecord>
}

class LabelRecord(
        val id: Int,
        val category: String?,
        val name: String,
        val description: String?,
        val color: String,
        val computedBy: String?
)
