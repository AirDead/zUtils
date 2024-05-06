package ru.airdead.zutils

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * Sends a packet to a player.
 *
 * @param packet The packet to be sent.
 * @throws IllegalArgumentException If the player is not a CraftPlayer.
 */
private fun Player.sendPacket(packet: Packet<*>) {
    val serverPlayer = (this as? CraftPlayer)?.handle
        ?: throw IllegalArgumentException("Player must be a CraftPlayer")

    try {
        serverPlayer.connection.send(packet)
    } catch (e: Exception) {
        println("Failed to send packet: ${e.message}")
    }
}

/**
 * Spawns a single entity for the player.
 *
 * This function takes an entity and sends a packet to the player to spawn it in the game world.
 * If the provided entity cannot be converted to a CraftEntity or is null, it logs an error message.
 *
 * @param entity The entity to be spawned.
 * @throws IllegalArgumentException If the player is not a CraftPlayer or if the entity cannot be converted to a CraftEntity.
 */
fun Player.spawnEntity(entity: Entity) {
    val craftEntity = (entity as? CraftEntity)
        ?: throw IllegalArgumentException("Invalid entity provided: $entity")

    val nmsEntity = craftEntity.handle

    val packet = try {
        ClientboundAddEntityPacket(nmsEntity)
    } catch (e: Exception) {
        println("Error creating packet: ${e.message}")
        return
    }

    this.sendPacket(packet)
}

/**
 * Spawns multiple entities for the player.
 *
 * This function takes a list of entities and sends packets to the player to spawn them in the game world.
 * If an error occurs while creating the packet for any entity, it logs the error message.
 *
 * @param entities A list of entities to be spawned.
 * @throws IllegalArgumentException If the player is not a CraftPlayer.
 */
fun Player.spawnEntities(entities: List<Entity>) {
    entities.forEach { entity -> this.spawnEntity(entity) }
}
