package cloud.mallne.dicentra.synapse.model

import kotlinx.serialization.Serializable

@Serializable
data class Point<T : Number>(
    val x: T,
    val y: T
) {
    override fun toString(): String {
        return "($x, $y)"
    }
}

