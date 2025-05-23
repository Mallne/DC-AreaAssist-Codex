package cloud.mallne.dicentra.areaassist.synapse.exceptions

open class DiCentraException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(nested: Exception) : this(nested.message, nested.cause)
}