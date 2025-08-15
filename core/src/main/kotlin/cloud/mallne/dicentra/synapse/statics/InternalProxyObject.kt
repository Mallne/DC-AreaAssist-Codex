package cloud.mallne.dicentra.synapse.statics

@MustBeDocumented
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPEALIAS
)
@RequiresOptIn("This Property should not be used outside its own class")
@Retention(AnnotationRetention.BINARY)
annotation class InternalProxyObject
