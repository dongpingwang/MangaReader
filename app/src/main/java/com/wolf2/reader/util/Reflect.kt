package com.wolf2.reader.util

import java.lang.reflect.Field
import java.lang.reflect.Method

object Reflect {

    private var instance: Any? = null
    private var clazz: Class<*>? = null
    private var targetField: Field? = null
    private var targetMethod: Method? = null

    private fun reset() {
        instance = null
        clazz = null
        targetField = null
        targetMethod = null
    }

    fun on(className: String, instance: Any? = null): Reflect {
        reset()
        Reflect.instance = instance
        clazz = Class.forName(className)
        return this
    }

    fun on(clazz: Class<*>, instance: Any? = null): Reflect {
        reset()
        Reflect.instance = instance
        Reflect.clazz = clazz
        return this
    }

    fun onInstance(instance: Any, clazz: Class<*> = instance.javaClass): Reflect {
        reset()
        Reflect.instance = instance
        Reflect.clazz = clazz
        return this
    }

    fun field(fieldName: String): Reflect {
        targetField = kotlin.runCatching {
            clazz!!.getDeclaredField(fieldName).apply {
                isAccessible = true
            }
        }.onFailure { it.printStackTrace() }.getOrNull()
        return this
    }

    fun get(): Any? {
        requireNotNull(clazz)
        return kotlin.runCatching {
            targetField?.get(instance)
        }.onFailure { it.printStackTrace() }.getOrNull()
    }

    fun set(arg: Any?): Any? {
        requireNotNull(clazz)
        kotlin.runCatching {
            targetField?.set(instance, arg)
        }.exceptionOrNull()?.printStackTrace()
        return get()
    }

    fun method(methodName: String, vararg parameterTypes: Class<*>): Reflect {
        targetMethod = kotlin.runCatching {
            clazz!!.getDeclaredMethod(methodName, *parameterTypes).apply {
                isAccessible = true
            }
        }.onFailure { it.printStackTrace() }.getOrNull()
        return this
    }

    fun invoke(
        vararg args: Any?
    ): Any? {
        val _args = args.toList().toTypedArray()
        requireNotNull(clazz)
        return kotlin.runCatching {
            targetMethod?.invoke(
                instance,
                *_args
            )
        }.onFailure { it.printStackTrace() }.getOrNull()
    }

}
