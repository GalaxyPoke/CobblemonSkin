package com.example.cobblemon_skin.network

import com.example.cobblemon_skin.CobblemonSkinMod
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/** S2C packet: server pushes the list of registered skin IDs to the client. */
data class SkinListPayload(val skins: List<String>) : CustomPacketPayload {

    companion object {
        val ID: CustomPacketPayload.Type<SkinListPayload> = CustomPacketPayload.Type(
            ResourceLocation.fromNamespaceAndPath(CobblemonSkinMod.MOD_ID, "skin_list")
        )

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SkinListPayload> =
            object : StreamCodec<RegistryFriendlyByteBuf, SkinListPayload> {
                override fun decode(buf: RegistryFriendlyByteBuf): SkinListPayload {
                    val count = buf.readVarInt()
                    val skins = (0 until count).map { buf.readUtf() }
                    return SkinListPayload(skins)
                }
                override fun encode(buf: RegistryFriendlyByteBuf, value: SkinListPayload) {
                    buf.writeVarInt(value.skins.size)
                    value.skins.forEach { buf.writeUtf(it) }
                }
            }
    }

    override fun type(): CustomPacketPayload.Type<SkinListPayload> = ID
}

object SkinPackets {

    /**
     * Called from [CobblemonSkinMod.onInitialize] (common side).
     * Registers the S2C payload type and the JOIN event that sends skin list on connect.
     */
    fun registerCommon() {
        PayloadTypeRegistry.playS2C().register(SkinListPayload.ID, SkinListPayload.STREAM_CODEC)
    }

    /** Manually push the skin list to a specific player (call from a command if needed). */
    fun sendSkinList(player: ServerPlayer) {
        val skins = CobblemonSkinMod.registeredSkins.toList()
        try {
            ServerPlayNetworking.send(player, SkinListPayload(skins))
        } catch (e: Exception) {
            CobblemonSkinMod.LOGGER.debug(
                "CobblemonSkin: skin list send skipped for ${player.name.string} (${e.message})"
            )
        }
    }
}
