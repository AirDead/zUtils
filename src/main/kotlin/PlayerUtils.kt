package me.airdead.zutils

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.entity.Entity
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player

/**
 * Helper function to get the server player from a CraftPlayer.
 *
 * @throws IllegalArgumentException If the player is not a CraftPlayer.
 */
fun Player.getServerPlayer() = (this as? CraftPlayer)?.handle
    ?: throw IllegalArgumentException("Player must be a CraftPlayer")

/**
 * Sends a packet to a player.
 *
 * @param packet The packet to be sent.
 */
fun Player.sendPacket(packet: Packet<*>) {
    try {
        getServerPlayer().connection.send(packet)
    } catch (e: Exception) {
        println("Failed to send packet to player '${this.name}': ${e.message}")
    }
}

/**
 * Sends a list of packets to a player.
 *
 * @param packets The list of packets to be sent.
 */
fun Player.sendPackets(packets: List<Packet<*>>) {
    packets.forEach { packet ->
        try {
            getServerPlayer().connection.send(packet)
        } catch (e: Exception) {
            println("Failed to send packet to player '${this.name}': ${e.message}")
        }
    }
}

/**
 * Spawns an entity for the player.
 *
 * @param entity The entity to spawn.
 */
fun Player.spawnEntity(entity: Entity) {
    try {
        this.sendPacket(ClientboundAddEntityPacket(entity))
    } catch (e: Exception) {
        println("Error creating packet to spawn entity for player '${this.name}': ${e.message}")
    }
}

/**
 * Spawns a list of entities for the player.
 *
 * @param entities The list of entities to spawn.
 */
fun Player.spawnEntities(entities: List<Entity>) {
    val packets = entities.mapNotNull { entity ->
        try {
            ClientboundAddEntityPacket(entity)
        } catch (e: Exception) {
            println("Error creating packet to spawn entity for player '${this.name}': ${e.message}")
            null
        }
    }
    this.sendPackets(packets)
}

