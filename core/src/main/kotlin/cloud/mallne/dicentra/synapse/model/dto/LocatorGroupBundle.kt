package cloud.mallne.dicentra.synapse.model.dto

import cloud.mallne.dicentra.aviator.model.ServiceLocator

data class LocatorGroupBundle(
    private val value: List<Pair<APIServiceDTO, List<ServiceLocator>>>
) : List<Pair<APIServiceDTO, List<ServiceLocator>>> by value {
    val services
        get() = value.map { it.first }

    val locators get() = value.flatMap { it.second }

    fun hasCollisions(): Boolean {
        val comparators = locators.map { it.toString() }
        return comparators.size != comparators.distinct().size
    }

    fun collisions(): LocatorGroupBundle {
        val locatorStrings = locators.map { it.toString() }
        val collidingStrings = locatorStrings.groupBy { it }.filter { it.value.size > 1 }.keys
        val collidingServices = value.filter { pair ->
            pair.second.any { locator ->
                collidingStrings.contains(locator.toString())
            }
        }
        return LocatorGroupBundle(collidingServices)
    }
}