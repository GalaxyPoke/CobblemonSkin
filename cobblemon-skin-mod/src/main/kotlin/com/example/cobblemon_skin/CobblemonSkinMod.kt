package com.example.cobblemon_skin

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.example.cobblemon_skin.command.SkinCommand
import com.example.cobblemon_skin.config.SkinConfig
import com.example.cobblemon_skin.config.UiConfig
import com.example.cobblemon_skin.loader.SkinPackLoader
import com.example.cobblemon_skin.network.SkinPackets
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

object CobblemonSkinMod : ModInitializer {

    const val MOD_ID = "cobblemon_skin"
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    /**
     * All skin IDs that are registered and available for use.
     * Other mods can call CobblemonSkinMod.registerSkin("my_skin_id") to add skins.
     * The corresponding variation JSON must exist in:
     *   assets/cobblemon/bedrock/pokemon/variations/<skinId>.json
     */
    val registeredSkins = mutableSetOf<String>()

    /** Maps skinId → species ResourceLocation string (e.g. "cobblemon:gyarados") */
    val skinSpeciesMap = mutableMapOf<String, String>()

    /** Per-skin UI rendering config (scale, offsetX, offsetY) */
    data class SkinUiConfig(val scale: Float = 1.0f, val offsetX: Int = 0, val offsetY: Int = 0)
    val skinUiConfigs = mutableMapOf<String, SkinUiConfig>()

    /** Per-skin metadata (description, quality, obtain method) */
    data class SkinMeta(
        val description: String = "",
        val quality: String = "普通",
        val obtain: String = "",
        val detail: String = ""
    )
    val skinMetaMap = mutableMapOf<String, SkinMeta>()

    override fun onInitialize() {
        LOGGER.info("CobblemonSkin mod initializing...")

        // Register packet codecs on BOTH sides (needed for channel negotiation)
        SkinPackets.registerCommon()

        val isDedicatedServer = FabricLoader.getInstance().environmentType == EnvType.SERVER
        if (isDedicatedServer) {
            LOGGER.info("Running on dedicated server — loading skin packs...")
            com.example.cobblemon_skin.server.SkinManager.loadAll()
            SkinPackLoader.loadAll()
            SkinConfig.load()
            com.example.cobblemon_skin.server.ResourcePackTransfer.prepare()

            // Register server-side packet handlers (tick-based chunk sender + C2S receivers)
            // NOTE: Skin list sending is triggered by Bukkit plugin's PlayerJoinEvent, NOT here
            com.example.cobblemon_skin.server.ServerPacketHandler.register()

            CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
                SkinCommand.register(dispatcher)
            }

            LOGGER.info("CobblemonSkin server initialized. ${registeredSkins.size} skins registered.")
        } else {
            LOGGER.info("Running on client — packet codecs registered, waiting for server data.")
        }
    }

    fun registerSkin(skinId: String) {
        registeredSkins.add(skinId)
    }

    /** Aspect string prefix used for all skin aspects. */
    const val ASPECT_PREFIX = "skin_"

    /**
     * Returns the aspect string for a given skin ID.
     * e.g. "pikachu_black" → "skin_pikachu_black"
     */
    fun aspectFor(skinId: String) = "$ASPECT_PREFIX$skinId"

    /**
     * Applies a skin to the given Pokémon by adding it to forcedAspects.
     * Any previously applied skin will be removed first.
     * forcedAspects setter automatically calls updateAspects() and syncs to clients.
     */
    fun applySkin(pokemon: Pokemon, skinId: String) {
        val cleansed = pokemon.forcedAspects.filter { !it.startsWith(ASPECT_PREFIX) }.toSet()
        pokemon.forcedAspects = cleansed + aspectFor(skinId)
    }

    /**
     * Removes any applied skin from the given Pokémon.
     */
    fun clearSkin(pokemon: Pokemon) {
        pokemon.forcedAspects = pokemon.forcedAspects.filter { !it.startsWith(ASPECT_PREFIX) }.toSet()
    }

    /**
     * Returns the active skin ID on the given Pokémon, or null if none.
     */
    fun getActiveSkin(pokemon: Pokemon): String? {
        return pokemon.forcedAspects
            .firstOrNull { it.startsWith(ASPECT_PREFIX) }
            ?.removePrefix(ASPECT_PREFIX)
    }

    /**
     * Gets the Pokémon in a player's party slot (1-indexed, 1–6).
     * PartyStore.get(Int) directly returns Pokemon?, no slot wrapper.
     */
    fun getPartyPokemon(party: PlayerPartyStore, slot: Int): Pokemon? {
        if (slot < 1 || slot > 6) return null
        return party.get(slot - 1)
    }
}
