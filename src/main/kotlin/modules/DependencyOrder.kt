package me.airdead.zutils.modules

import kotlin.reflect.KClass

annotation class DependencyOrder(
    val before: Array<KClass<out PluginModule>> = [],
    val after: Array<KClass<out PluginModule>> = []
)