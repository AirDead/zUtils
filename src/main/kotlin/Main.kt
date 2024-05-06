package ru.airdead.zutils

import net.minecraft.world.entity.EntityType
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin(), CommandExecutor {

    override fun onEnable() {
        getCommand("test")?.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender is Player) {
            val worldServer = (sender.location.world as CraftWorld).handle
            val entity = EntityType.ZOMBIE.create(worldServer)
            entity?.moveTo(sender.location.x, sender.location.y, sender.location.z)
            if (entity != null) {
                sender.spawnEntity(entity)
            }
        }

        return true
    }

}