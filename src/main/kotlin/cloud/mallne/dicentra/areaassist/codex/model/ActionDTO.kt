package cloud.mallne.dicentra.areaassist.codex.model

import cloud.mallne.dicentra.areaassist.model.actions.ServersideAction
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ActionDTO @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class) constructor(
    @Transient
    val id: String = Uuid.random().toString(),
    val scope: String? = null,
    val action: ServersideAction,
    val expires: Instant = Clock.System.now().plus(14.days)
)