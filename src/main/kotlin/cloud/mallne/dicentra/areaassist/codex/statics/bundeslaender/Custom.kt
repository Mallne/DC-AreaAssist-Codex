package cloud.mallne.dicentra.areaassist.codex.statics.bundeslaender

import cloud.mallne.dicentra.areaassist.codex.model.Point

internal object Custom : BundeslandDefinition {
    override val roughBoundaries: List<Point<Double>>
        get() = listOf()
}
