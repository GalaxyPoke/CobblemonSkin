/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.net.messages.client.spawn

import com.cobblemon.mod.common.api.npc.NPCClasses
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.entity.npc.NPCPlayerModelType
import com.cobblemon.mod.common.entity.npc.NPCPlayerTexture
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readIdentifier
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.readText
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeIdentifier
import com.cobblemon.mod.common.util.writeString
import com.cobblemon.mod.common.util.writeText
import net.minecraft.client.multiplayer.ClientLevel
import java.util.UUID
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity

class SpawnNPCPacket(
    var npcClass: ResourceLocation,
    var resourceIdentifier: ResourceLocation,
    var aspects: Set<String>,
    var level: Int,
    var battleIds: Set<UUID>,
    var name: Component,
    var poseType: PoseType,
    var texture: NPCPlayerTexture,
    var hideNameTag: Boolean,
    var hitboxWidth: Float = 0.6F,
    var hitboxHeight: Float = 1.8F,
    var hitboxEyesHeight: Float = 1.62F,
    var hitboxScale: Float = 1F,
    var renderScale: Float = 1F,
    vanillaSpawnPacket: ClientboundAddEntityPacket
) : SpawnExtraDataEntityPacket<SpawnNPCPacket, NPCEntity>(vanillaSpawnPacket) {

    override val id: ResourceLocation = ID

    constructor(entity: NPCEntity, vanillaSpawnPacket: ClientboundAddEntityPacket) : this(
        entity.npc.id,
        entity.resourceIdentifier,
        entity.aspects,
        entity.level,
        entity.battleIds,
        entity.name,
        entity.entityData.get(NPCEntity.POSE_TYPE),
        entity.entityData.get(NPCEntity.NPC_PLAYER_TEXTURE),
        entity.hideNameTag,
        entity.hitboxWidth,
        entity.hitboxHeight,
        entity.hitboxEyesHeight,
        entity.hitboxScale,
        entity.renderScale,
        vanillaSpawnPacket
    )

    override fun encodeEntityData(buffer: RegistryFriendlyByteBuf) {
        buffer.writeIdentifier(this.npcClass)
        buffer.writeIdentifier(this.resourceIdentifier)
        buffer.writeCollection(this.aspects) { pb, value -> pb.writeString(value) }
        buffer.writeInt(this.level)
        buffer.writeCollection(this.battleIds) { pb, value -> pb.writeUUID(value) }
        buffer.writeText(name)
        buffer.writeEnumConstant(this.poseType)
        buffer.writeEnumConstant(this.texture.model)
        if (this.texture.model != NPCPlayerModelType.NONE) {
            buffer.writeByteArray(this.texture.texture)
        }
        buffer.writeBoolean(this.hideNameTag)
        buffer.writeFloat(hitboxWidth)
        buffer.writeFloat(hitboxHeight)
        buffer.writeFloat(hitboxEyesHeight)
        buffer.writeFloat(hitboxScale)
        buffer.writeFloat(renderScale)
    }

    override fun applyData(entity: NPCEntity, level: ClientLevel) {
        entity.npc = NPCClasses.getByIdentifier(this.npcClass) ?: error("received unknown NPCClass: $npcClass")
        if (entity.resourceIdentifier != this.resourceIdentifier) {
            entity.forcedResourceIdentifier = this.resourceIdentifier
        }
        entity.customName = name
        entity.entityData.set(NPCEntity.LEVEL, this.level)
        entity.entityData.set(NPCEntity.BATTLE_IDS, this.battleIds.toMutableSet())
        entity.entityData.set(NPCEntity.ASPECTS, aspects)
        entity.entityData.set(NPCEntity.POSE_TYPE, poseType)
        entity.entityData.set(NPCEntity.NPC_PLAYER_TEXTURE, texture)
        entity.entityData.set(NPCEntity.HIDE_NAME_TAG, this.hideNameTag)
        entity.hitboxWidth = this.hitboxWidth
        entity.hitboxHeight = this.hitboxHeight
        entity.hitboxEyesHeight = this.hitboxEyesHeight
        entity.hitboxScale = this.hitboxScale
        entity.renderScale = this.renderScale
    }

    override fun checkType(entity: Entity): Boolean = entity is NPCEntity

    companion object {
        val ID = cobblemonResource("spawn_npc_entity")
        fun decode(buffer: RegistryFriendlyByteBuf): SpawnNPCPacket {
            val npc = buffer.readIdentifier()
            val resourceIdentifier = buffer.readIdentifier()
            val aspects = buffer.readList { buffer.readString() }.toSet()
            val level = buffer.readInt()
            val battleIds = buffer.readList { buffer.readUUID() }.toSet()
            val name = buffer.readText()
            val poseType = buffer.readEnumConstant(PoseType::class.java)
            val model = buffer.readEnumConstant(NPCPlayerModelType::class.java)
            val texture = if (model != NPCPlayerModelType.NONE) {
                NPCPlayerTexture(buffer.readByteArray(), model)
            } else {
                NPCPlayerTexture(byteArrayOf(), model)
            }
            val hideNameTag = buffer.readBoolean()
            val hitboxWidth = buffer.readFloat()
            val hitboxHeight = buffer.readFloat()
            val hitboxEyesHeight = buffer.readFloat()
            val hitboxScale = buffer.readFloat()
            val renderScale = buffer.readFloat()
            val vanillaPacket = decodeVanillaPacket(buffer)

            return SpawnNPCPacket(npc, resourceIdentifier, aspects, level, battleIds, name, poseType, texture, hideNameTag, hitboxWidth, hitboxHeight, hitboxEyesHeight, hitboxScale, renderScale, vanillaPacket)
        }
    }

}
