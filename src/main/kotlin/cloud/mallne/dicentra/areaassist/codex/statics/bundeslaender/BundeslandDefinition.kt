package cloud.mallne.dicentra.areaassist.codex.statics.bundeslaender

import cloud.mallne.dicentra.areaassist.codex.model.Point

interface BundeslandDefinition {
    val roughBoundaries: List<Point<Double>>
}