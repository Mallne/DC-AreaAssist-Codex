package cloud.mallne.dicentra.areaassist.synapse.statics

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

object Validation {
    object Null {
        fun exactly(count: Int, vararg properties: Any?) =
            properties.count { it != null } == count

        fun atLeast(count: Int, vararg properties: Any?) =
            properties.count { it != null } >= count

        fun atMost(count: Int, vararg properties: Any?) =
            properties.count { it != null } <= count

        fun oneOrNone(vararg properties: Any?) = atMost(1, *properties)
        fun none(vararg properties: Any?) = exactly(0, *properties)
        fun one(vararg properties: Any?) = exactly(1, *properties)

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> exactlyOf(count: Int, instance: T) =
            exactly(count, *getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> atLeastOf(count: Int, instance: T) =
            atLeast(count, *getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> atMostOf(count: Int, instance: T) =
            atMost(count, *getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> oneOrNoneOf(instance: T) = oneOrNone(*getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> noneOf(instance: T) = none(*getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> oneOf(instance: T) = one(*getProperties(instance))

        @InternalProxyObject
        inline fun <reified T : Any> getProperties(instance: T): Array<Any?> {
            val clazz: KClass<T> = T::class
            val members = clazz.declaredMemberProperties
            return members.map {
                it.get(instance)
            }.toTypedArray()
        }
    }

    object Bool {
        fun exactly(count: Int, vararg properties: Boolean, toBe: Boolean = true) =
            properties.count { it == toBe } == count

        fun atLeast(count: Int, vararg properties: Boolean, toBe: Boolean = true) =
            properties.count { it == toBe } >= count

        fun atMost(count: Int, vararg properties: Boolean, toBe: Boolean = true) =
            properties.count { it == toBe } <= count

        fun oneOrNone(vararg properties: Boolean, toBe: Boolean = true) =
            atMost(1, toBe = toBe, properties = properties)

        fun none(vararg properties: Boolean, toBe: Boolean = true) = exactly(0, toBe = toBe, properties = properties)
        fun one(vararg properties: Boolean, toBe: Boolean = true) = exactly(1, toBe = toBe, properties = properties)

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> exactlyOf(count: Int, instance: T) =
            exactly(count, *getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> atLeastOf(count: Int, instance: T) =
            atLeast(count, *getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> atMostOf(count: Int, instance: T) =
            atMost(count, *getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> oneOrNoneOf(instance: T) = oneOrNone(*getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> noneOf(instance: T) = none(*getProperties(instance))

        @OptIn(InternalProxyObject::class)
        inline fun <reified T : Any> oneOf(instance: T) = one(*getProperties(instance))

        @InternalProxyObject
        inline fun <reified T : Any> getProperties(instance: T): BooleanArray {
            val clazz: KClass<T> = T::class
            val members = clazz.declaredMemberProperties
            return members.mapNotNull {
                val o = it.get(instance)
                o as? Boolean
            }.toBooleanArray()
        }
    }

}