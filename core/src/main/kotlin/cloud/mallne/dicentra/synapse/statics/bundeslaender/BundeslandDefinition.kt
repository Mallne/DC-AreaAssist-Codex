package cloud.mallne.dicentra.synapse.statics.bundeslaender

import cloud.mallne.dicentra.synapse.model.Point

interface BundeslandDefinition {
    val roughBoundaries: List<Point<Double>>
}