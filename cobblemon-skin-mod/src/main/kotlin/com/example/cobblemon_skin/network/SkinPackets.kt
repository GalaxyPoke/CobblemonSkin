package com.example.cobblemon_skin.network

import com.example.cobblemon_skin.CobblemonSkinMod
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

// ═══════════════════════════════════════════════════════════════════════════
//  DATA MODELS
// ═══════════════════════════════════════════════════════════════════════════

/** Lightweight skin metadata sent in the skin list (no binary data). */
data class SkinInfo(
    val skinId: String,
    val species: String,       // e.g. "cobblemon:pikachu"
    val displayName: String,   // human-readable name
    val quality: String,       // 普通/稀有/史诗/传说
    val description: String
)

/** Per-skin resource files sent on demand (models, textures, posers, animations). */
data class SkinResourceData(
    val skinId: String,
    val files: Map<String, ByteArray>  // relativePath → content
) {
    override fun equals(other: Any?) = other is SkinResourceData && skinId == other.skinId
    override fun hashCode() = skinId.hashCode()
}

// ═══════════════════════════════════════════════════════════════════════════
//  S2C PACKETS (Server → Client)
// ═══════════════════════════════════════════════════════════════════════════

/** S2C: Server sends full skin list metadata + resource pack hash on player join. */
data class SkinListPayload(val skins: List<SkinInfo>, val packHash: String) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<SkinListPayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "skin_list")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkinListPayload> =
            object : StreamCodec<RegistryFriendlyByteBuf, SkinListPayload> {
                override fun decode(buf: RegistryFriendlyByteBuf): SkinListPayload {
                    val packHash = buf.readUtf()
                    val count = buf.readVarInt()
                    val skins = (0 until count).map {
                        SkinInfo(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf())
                    }
                    return SkinListPayload(skins, packHash)
                }
                override fun encode(buf: RegistryFriendlyByteBuf, value: SkinListPayload) {
                    buf.writeUtf(value.packHash)
                    buf.writeVarInt(value.skins.size)
                    value.skins.forEach { s ->
                        buf.writeUtf(s.skinId); buf.writeUtf(s.species); buf.writeUtf(s.displayName)
                        buf.writeUtf(s.quality); buf.writeUtf(s.description)
                    }
                }
            }
    }
    override fun type(): CustomPacketPayload.Type<SkinListPayload> = ID
}

/** S2C: Server sends per-skin resource files (on-demand, response to client request). */
data class SkinResourcePayload(val data: SkinResourceData) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<SkinResourcePayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "skin_resource")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkinResourcePayload> =
            object : StreamCodec<RegistryFriendlyByteBuf, SkinResourcePayload> {
                override fun decode(buf: RegistryFriendlyByteBuf): SkinResourcePayload {
                    val skinId = buf.readUtf()
                    val fileCount = buf.readVarInt()
                    val files = (0 until fileCount).associate { buf.readUtf() to buf.readByteArray() }
                    return SkinResourcePayload(SkinResourceData(skinId, files))
                }
                override fun encode(buf: RegistryFriendlyByteBuf, value: SkinResourcePayload) {
                    buf.writeUtf(value.data.skinId)
                    buf.writeVarInt(value.data.files.size)
                    value.data.files.forEach { (path, bytes) ->
                        buf.writeUtf(path)
                        buf.writeByteArray(bytes)
                    }
                }
            }
    }
    override fun type(): CustomPacketPayload.Type<SkinResourcePayload> = ID
}

/** S2C: Server broadcasts a skin apply/clear event to all players. */
data class SkinApplyBroadcast(
    val playerUUID: java.util.UUID,
    val slot: Int,           // 1-6
    val skinId: String       // empty = clear
) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<SkinApplyBroadcast> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "skin_apply_broadcast")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkinApplyBroadcast> =
            object : StreamCodec<RegistryFriendlyByteBuf, SkinApplyBroadcast> {
                override fun decode(buf: RegistryFriendlyByteBuf) =
                    SkinApplyBroadcast(buf.readUUID(), buf.readVarInt(), buf.readUtf())
                override fun encode(buf: RegistryFriendlyByteBuf, value: SkinApplyBroadcast) {
                    buf.writeUUID(value.playerUUID); buf.writeVarInt(value.slot); buf.writeUtf(value.skinId)
                }
            }
    }
    override fun type(): CustomPacketPayload.Type<SkinApplyBroadcast> = ID
}

// ═══════════════════════════════════════════════════════════════════════════
//  C2S PACKETS (Client → Server)
// ═══════════════════════════════════════════════════════════════════════════

/** C2S: Client requests skin resource data for a specific skin. */
data class SkinResourceRequest(val skinId: String) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<SkinResourceRequest> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "skin_resource_req")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkinResourceRequest> =
            object : StreamCodec<RegistryFriendlyByteBuf, SkinResourceRequest> {
                override fun decode(buf: RegistryFriendlyByteBuf) = SkinResourceRequest(buf.readUtf())
                override fun encode(buf: RegistryFriendlyByteBuf, value: SkinResourceRequest) { buf.writeUtf(value.skinId) }
            }
    }
    override fun type(): CustomPacketPayload.Type<SkinResourceRequest> = ID
}

/** C2S: Client requests to apply a skin to a party slot. */
data class SkinApplyRequest(val slot: Int, val skinId: String) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<SkinApplyRequest> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "skin_apply_req")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkinApplyRequest> =
            object : StreamCodec<RegistryFriendlyByteBuf, SkinApplyRequest> {
                override fun decode(buf: RegistryFriendlyByteBuf) = SkinApplyRequest(buf.readVarInt(), buf.readUtf())
                override fun encode(buf: RegistryFriendlyByteBuf, value: SkinApplyRequest) {
                    buf.writeVarInt(value.slot); buf.writeUtf(value.skinId)
                }
            }
    }
    override fun type(): CustomPacketPayload.Type<SkinApplyRequest> = ID
}

/** C2S: Client requests to clear skin from a party slot. */
data class SkinClearRequest(val slot: Int) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<SkinClearRequest> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "skin_clear_req")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkinClearRequest> =
            object : StreamCodec<RegistryFriendlyByteBuf, SkinClearRequest> {
                override fun decode(buf: RegistryFriendlyByteBuf) = SkinClearRequest(buf.readVarInt())
                override fun encode(buf: RegistryFriendlyByteBuf, value: SkinClearRequest) { buf.writeVarInt(value.slot) }
            }
    }
    override fun type(): CustomPacketPayload.Type<SkinClearRequest> = ID
}

/** C2S: Client requests the full resource pack download. */
class ResourcePackRequestC2S : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<ResourcePackRequestC2S> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "respack_request")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ResourcePackRequestC2S> =
            object : StreamCodec<RegistryFriendlyByteBuf, ResourcePackRequestC2S> {
                override fun decode(buf: RegistryFriendlyByteBuf) = ResourcePackRequestC2S()
                override fun encode(buf: RegistryFriendlyByteBuf, value: ResourcePackRequestC2S) {}
            }
    }
    override fun type(): CustomPacketPayload.Type<ResourcePackRequestC2S> = ID
}

/** S2C: Server sends a chunk of the resource pack ZIP. */
data class ResourcePackChunkS2C(
    val chunkIndex: Int,
    val totalChunks: Int,
    val data: ByteArray
) : CustomPacketPayload {
    companion object {
        val ID: CustomPacketPayload.Type<ResourcePackChunkS2C> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "respack_chunk")
        )
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ResourcePackChunkS2C> =
            object : StreamCodec<RegistryFriendlyByteBuf, ResourcePackChunkS2C> {
                override fun decode(buf: RegistryFriendlyByteBuf): ResourcePackChunkS2C {
                    return ResourcePackChunkS2C(buf.readVarInt(), buf.readVarInt(), buf.readByteArray())
                }
                override fun encode(buf: RegistryFriendlyByteBuf, value: ResourcePackChunkS2C) {
                    buf.writeVarInt(value.chunkIndex)
                    buf.writeVarInt(value.totalChunks)
                    buf.writeByteArray(value.data)
                }
            }
    }
    override fun type(): CustomPacketPayload.Type<ResourcePackChunkS2C> = ID
    override fun equals(other: Any?) = other is ResourcePackChunkS2C && chunkIndex == other.chunkIndex
    override fun hashCode() = chunkIndex
}

// ═══════════════════════════════════════════════════════════════════════════
//  REGISTRATION
// ═══════════════════════════════════════════════════════════════════════════

object SkinPackets {
    fun registerCommon() {
        // S2C packets
        PayloadTypeRegistry.playS2C().register(SkinListPayload.ID, SkinListPayload.STREAM_CODEC)
        PayloadTypeRegistry.playS2C().register(SkinResourcePayload.ID, SkinResourcePayload.STREAM_CODEC)
        PayloadTypeRegistry.playS2C().register(SkinApplyBroadcast.ID, SkinApplyBroadcast.STREAM_CODEC)

        // S2C: resource pack chunk
        PayloadTypeRegistry.playS2C().register(ResourcePackChunkS2C.ID, ResourcePackChunkS2C.STREAM_CODEC)

        // C2S packets
        PayloadTypeRegistry.playC2S().register(SkinResourceRequest.ID, SkinResourceRequest.STREAM_CODEC)
        PayloadTypeRegistry.playC2S().register(SkinApplyRequest.ID, SkinApplyRequest.STREAM_CODEC)
        PayloadTypeRegistry.playC2S().register(SkinClearRequest.ID, SkinClearRequest.STREAM_CODEC)
        PayloadTypeRegistry.playC2S().register(ResourcePackRequestC2S.ID, ResourcePackRequestC2S.STREAM_CODEC)
    }

    fun sendSkinList(player: ServerPlayer, skins: List<SkinInfo>, packHash: String) {
        try {
            ServerPlayNetworking.send(player, SkinListPayload(skins, packHash))
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.debug("Skin list send skipped for ${player.name.string}: ${e.message}")
        }
    }

    fun sendSkinResource(player: ServerPlayer, data: SkinResourceData) {
        try {
            ServerPlayNetworking.send(player, SkinResourcePayload(data))
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.warn("Skin resource send failed for ${data.skinId}: ${e.message}")
        }
    }

    fun broadcastSkinApply(server: net.minecraft.server.MinecraftServer, playerUUID: java.util.UUID, slot: Int, skinId: String) {
        val payload = SkinApplyBroadcast(playerUUID, slot, skinId)
        server.playerList.players.forEach { player ->
            try { ServerPlayNetworking.send(player, payload) } catch (_: Exception) {}
        }
    }
}
