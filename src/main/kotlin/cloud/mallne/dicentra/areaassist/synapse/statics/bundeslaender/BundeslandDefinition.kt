package cloud.mallne.dicentra.areaassist.synapse.statics.bundeslaender

import cloud.mallne.dicentra.areaassist.synapse.model.Point

interface BundeslandDefinition {
    val roughBoundaries: List<Point<Double>>
}