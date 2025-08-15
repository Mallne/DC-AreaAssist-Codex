package cloud.mallne.dicentra.synapse.helper

fun String.toBooleanish(): Boolean? {
    val upper = this.uppercase()
    if (upper == "TRUE" || upper == "YES") return true
    if (upper == "FALSE" || upper == "NO") return false
    return null
}