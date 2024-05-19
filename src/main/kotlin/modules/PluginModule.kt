package me.airdead.zutils.modules

import me.airdead.zutils.global.minecraft.ServerPlugin

interface PluginModule {
    val id: String
    fun onLoad(plugin: ServerPlugin)
    fun onUnload(plugin: ServerPlugin)
}