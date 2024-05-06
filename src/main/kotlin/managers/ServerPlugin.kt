package me.airdead.zutils.managers

import me.airdead.zutils.listeners.CustomPlayerMoveEvent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.collections.HashMap

abstract class ServerPlugin : JavaPlugin() {
    private var eventLocations: HashMap<UUID, Location> = hashMapOf()
    abstract val components: List<Any>

    override fun onEnable() {
        loadComponents()
        setupMovementChecker()
    }


    private fun loadComponents() {
        components.forEach { component ->
            when (component) {
                is Listener -> register(component)
                is ServerCommand -> register(component)
            }
        }
    }

    private fun register(listener: Listener) {
        server.pluginManager.registerEvents(listener, this)
    }

    private fun register(command: ServerCommand) {
        val bukkitCommand = getCommand(command.name)
        if(bukkitCommand != null) {
            bukkitCommand.setExecutor(command)
            bukkitCommand.tabCompleter = command
        }
    }

    private fun setupMovementChecker() {
        server.scheduler.scheduleSyncRepeatingTask(this, ::playerMove, 0L, 10L)
    }

    private fun playerMove() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val currentLocation = player.location
            val lastLocation = eventLocations[player.uniqueId]

            if (lastLocation == null) {
                eventLocations[player.uniqueId] = currentLocation.clone()
            } else if (lastLocation.blockX != currentLocation.blockX ||
                lastLocation.blockY != currentLocation.blockY ||
                lastLocation.blockZ != currentLocation.blockZ) {
                val event = CustomPlayerMoveEvent(player, lastLocation, currentLocation)
                Bukkit.getPluginManager().callEvent(event)

                if (event.isCancelled) {
                    player.teleport(lastLocation)
                } else {
                    eventLocations[player.uniqueId] = currentLocation.clone()
                }
            }
        }
    }

}