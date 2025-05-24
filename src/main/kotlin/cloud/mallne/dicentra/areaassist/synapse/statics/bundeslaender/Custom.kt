package cloud.mallne.dicentra.areaassist.synapse.statics.bundeslaender

import cloud.mallne.dicentra.areaassist.synapse.model.Point

internal object Custom : BundeslandDefinition {
    override val roughBoundaries: List<Point<Double>>
        get() = listOf()
}
