package cloud.mallne.dicentra.synapse.statics.bundeslaender

import cloud.mallne.dicentra.synapse.model.Point

internal object Custom : BundeslandDefinition {
    override val roughBoundaries: List<Point<Double>>
        get() = listOf()
}
