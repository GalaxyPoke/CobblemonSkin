/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokeball

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.pokeball.catching.CaptureEffect
import com.cobblemon.mod.common.api.pokeball.catching.CatchRateModifier
import com.cobblemon.mod.common.api.pokeball.catching.effects.CaptureEffects
import com.cobblemon.mod.common.api.pokeball.catching.effects.FriendshipEarningBoostEffect
import com.cobblemon.mod.common.api.pokeball.catching.modifiers.*
import com.cobblemon.mod.common.api.pokemon.labels.CobblemonPokemonLabels
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.pokeball.PokeBall
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import kotlin.math.roundToInt

/**
 * The data registry for [PokeBall]s.
 * All the pokeball fields are guaranteed to exist
 */
object PokeBalls : JsonDataRegistry<PokeBall> {

    override val id = cobblemonResource("pokeballs")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<PokeBalls>()

    // ToDo once datapack pokeball is implemented add required adapters here
    override val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()
    override val typeToken: TypeToken<PokeBall> = TypeToken.get(PokeBall::class.java)
    override val resourcePath = "pokeballs"

    private val defaults = hashMapOf<ResourceLocation, PokeBall>()
    // ToDo datapack pokeball type here instead
    private val custom = hashMapOf<ResourceLocation, PokeBall>()

    @JvmStatic
    @get:JvmName("getPokeBall")
    val POKE_BALL
        get() = this.byName("poke_ball")
    @JvmStatic
    @get:JvmName("getSlateBall")
    val SLATE_BALL
        get() = this.byName("slate_ball")
    @JvmStatic
    @get:JvmName("getAzureBall")
    val AZURE_BALL
        get() = this.byName("azure_ball")
    @JvmStatic
    @get:JvmName("getVerdantBall")
    val VERDANT_BALL
        get() = this.byName("verdant_ball")
    @JvmStatic
    @get:JvmName("getRoseateBall")
    val ROSEATE_BALL
        get() = this.byName("roseate_ball")
    @JvmStatic
    @get:JvmName("getCitrineBall")
    val CITRINE_BALL
        get() = this.byName("citrine_ball")
    @JvmStatic
    @get:JvmName("getGreatBall")
    val GREAT_BALL
        get() = this.byName("great_ball")
    @JvmStatic
    @get:JvmName("getUltraBall")
    val ULTRA_BALL
        get() = this.byName("ultra_ball")
    @JvmStatic
    @get:JvmName("getMasterBall")
    val MASTER_BALL
        get() = this.byName("master_ball")
    @JvmStatic
    @get:JvmName("getSafariBall")
    val SAFARI_BALL
        get() = this.byName("safari_ball")
    @JvmStatic
    @get:JvmName("getFastBall")
    val FAST_BALL
        get() = this.byName("fast_ball")
    @JvmStatic
    @get:JvmName("getLevelBall")
    val LEVEL_BALL
        get() = this.byName("level_ball")
    @JvmStatic
    @get:JvmName("getLureBall")
    val LURE_BALL
        get() = this.byName("lure_ball")
    @JvmStatic
    @get:JvmName("getHeavyBall")
    val HEAVY_BALL
        get() = this.byName("heavy_ball")
    @JvmStatic
    @get:JvmName("getLoveBall")
    val LOVE_BALL
        get() = this.byName("love_ball")
    @JvmStatic
    @get:JvmName("getFriendBall")
    val FRIEND_BALL
        get() = this.byName("friend_ball")
    @JvmStatic
    @get:JvmName("getMoonBall")
    val MOON_BALL
        get() = this.byName("moon_ball")
    @JvmStatic
    @get:JvmName("getSportBall")
    val SPORT_BALL
        get() = this.byName("sport_ball")
    @JvmStatic
    @get:JvmName("getNetBall")
    val NET_BALL
        get() = this.byName("net_ball")
    @JvmStatic
    @get:JvmName("getDiveBall")
    val DIVE_BALL
        get() = this.byName("dive_ball")
    @JvmStatic
    @get:JvmName("getNestBall")
    val NEST_BALL
        get() = this.byName("nest_ball")
    @JvmStatic
    @get:JvmName("getRepeatBall")
    val REPEAT_BALL
        get() = this.byName("repeat_ball")
    @JvmStatic
    @get:JvmName("getTimerBall")
    val TIMER_BALL
        get() = this.byName("timer_ball")
    @JvmStatic
    @get:JvmName("getLuxuryBall")
    val LUXURY_BALL
        get() = this.byName("luxury_ball")
    @JvmStatic
    @get:JvmName("getPremierBall")
    val PREMIER_BALL
        get() = this.byName("premier_ball")
    @JvmStatic
    @get:JvmName("getDuskBall")
    val DUSK_BALL
        get() = this.byName("dusk_ball")
    @JvmStatic
    @get:JvmName("getHealBall")
    val HEAL_BALL
        get() = this.byName("heal_ball")
    @JvmStatic
    @get:JvmName("getQuickBall")
    val QUICK_BALL
        get() = this.byName("quick_ball")
    @JvmStatic
    @get:JvmName("getCherishBall")
    val CHERISH_BALL
        get() = this.byName("cherish_ball")
    @JvmStatic
    @get:JvmName("getParkBall")
    val PARK_BALL
        get() = this.byName("park_ball")
    @JvmStatic
    @get:JvmName("getDreamBall")
    val DREAM_BALL
        get() = this.byName("dream_ball")
    @JvmStatic
    @get:JvmName("getBeastBall")
    val BEAST_BALL
        get() = this.byName("beast_ball")
    @JvmStatic
    @get:JvmName("getAncientPokeBall")
    val ANCIENT_POKE_BALL
        get() = this.byName("ancient_poke_ball")
    @JvmStatic
    @get:JvmName("getAncientCitrineBall")
    val ANCIENT_CITRINE_BALL
        get() = this.byName("ancient_citrine_ball")
    @JvmStatic
    @get:JvmName("getAncientVerdantBall")
    val ANCIENT_VERDANT_BALL
        get() = this.byName("ancient_verdant_ball")
    @JvmStatic
    @get:JvmName("getAncientAzureBall")
    val ANCIENT_AZURE_BALL
        get() = this.byName("ancient_azure_ball")
    @JvmStatic
    @get:JvmName("getAncientRoseateBall")
    val ANCIENT_ROSEATE_BALL
        get() = this.byName("ancient_roseate_ball")
    @JvmStatic
    @get:JvmName("getAncientSlateBall")
    val ANCIENT_SLATE_BALL
        get() = this.byName("ancient_slate_ball")
    @JvmStatic
    @get:JvmName("getAncientIvoryBall")
    val ANCIENT_IVORY_BALL
        get() = this.byName("ancient_ivory_ball")
    @JvmStatic
    @get:JvmName("getAncientGreatBall")
    val ANCIENT_GREAT_BALL
        get() = this.byName("ancient_great_ball")
    @JvmStatic
    @get:JvmName("getAncientUltraBall")
    val ANCIENT_ULTRA_BALL
        get() = this.byName("ancient_ultra_ball")
    @JvmStatic
    @get:JvmName("getAncientHeavyBall")
    val ANCIENT_HEAVY_BALL
        get() = this.byName("ancient_heavy_ball")
    @JvmStatic
    @get:JvmName("getAncientLeadenBall")
    val ANCIENT_LEADEN_BALL
        get() = this.byName("ancient_leaden_ball")
    @JvmStatic
    @get:JvmName("getAncientGigatonBall")
    val ANCIENT_GIGATON_BALL
        get() = this.byName("ancient_gigaton_ball")
    @JvmStatic
    @get:JvmName("getAncientFeatherBall")
    val ANCIENT_FEATHER_BALL
        get() = this.byName("ancient_feather_ball")
    @JvmStatic
    @get:JvmName("getAncientWingBall")
    val ANCIENT_WING_BALL
        get() = this.byName("ancient_wing_ball")
    @JvmStatic
    @get:JvmName("getAncientJetBall")
    val ANCIENT_JET_BALL
        get() = this.byName("ancient_jet_ball")
    @JvmStatic
    @get:JvmName("getAncientOriginBall")
    val ANCIENT_ORIGIN_BALL
        get() = this.byName("ancient_origin_ball")

    init {
        createDefault("poke_ball")
        createDefault("slate_ball")
        createDefault("azure_ball")
        createDefault("verdant_ball")
        createDefault("roseate_ball")
        createDefault("citrine_ball")
        createDefault("great_ball", MultiplierModifier(1.5F))
        createDefault("ultra_ball", MultiplierModifier(2F))
        createDefault("master_ball", GuaranteedModifier())
        createDefault("safari_ball", CatchRateModifiers.SAFARI)
        createDefault("fast_ball", BaseStatModifier(Stats.SPEED, { it >= 100 }, 4F))
        createDefault("level_ball", CatchRateModifiers.LEVEL)
        createDefault("lure_ball", CatchRateModifiers.LURE)
        createDefault("heavy_ball", CatchRateModifiers.WEIGHT_BASED)
        createDefault("love_ball", CatchRateModifiers.LOVE)
        createDefault("friend_ball", effects = listOf(CaptureEffects.friendshipSetter(150)))
        createDefault("moon_ball", CatchRateModifiers.MOON_PHASES)
        createDefault("sport_ball", MultiplierModifier(1.5F))
        createDefault("net_ball", CatchRateModifiers.typeBoosting(3F, ElementalTypes.BUG, ElementalTypes.WATER))
        createDefault("dive_ball", CatchRateModifiers.SUBMERGED_IN_WATER, waterDragValue = 0.99F)
        createDefault("nest_ball", CatchRateModifiers.NEST)
        createDefault("repeat_ball", CatchRateModifiers.REPEAT)
        createDefault("timer_ball", CatchRateModifiers.turnBased { turn -> (1F * turn * (1229F / 4096F)).coerceAtMost(4F) })
        createDefault("luxury_ball", effects = listOf(FriendshipEarningBoostEffect(2F)))
        createDefault("premier_ball")
        createDefault("dusk_ball", CatchRateModifiers.LIGHT_LEVEL)
        createDefault("heal_ball", effects = listOf(CaptureEffects.FULL_RESTORE))
        createDefault("quick_ball", CatchRateModifiers.turnBased { turn -> if (turn == 1) 5F else 1F })
        createDefault("cherish_ball")
        createDefault("park_ball", CatchRateModifiers.PARK)
        createDefault("dream_ball", CatchRateModifiers.statusBoosting(4F, Statuses.SLEEP))
        createDefault("beast_ball", LabelModifier(5F, true, CobblemonPokemonLabels.ULTRA_BEAST)/*, LabelModifier(0.1F, false, CobblemonPokemonLabels.ULTRA_BEAST))*/)
        createDefault("ancient_poke_ball", ancient = true)
        createDefault("ancient_citrine_ball", ancient = true)
        createDefault("ancient_verdant_ball", ancient = true)
        createDefault("ancient_azure_ball", ancient = true)
        createDefault("ancient_roseate_ball", ancient = true)
        createDefault("ancient_slate_ball", ancient = true)
        createDefault("ancient_ivory_ball", ancient = true)
        createDefault("ancient_great_ball", MultiplierModifier(1.5F), ancient = true)
        createDefault("ancient_ultra_ball", MultiplierModifier(2F), ancient = true)
        createDefault("ancient_heavy_ball", throwPower = 0.75f, ancient = true)
        createDefault("ancient_leaden_ball", throwPower = 0.75f, ancient = true)
        createDefault("ancient_gigaton_ball", throwPower = 0.75f, ancient = true)
        createDefault("ancient_feather_ball", throwPower = 2.5f, ancient = true)
        createDefault("ancient_wing_ball", throwPower = 2.5f, ancient = true)
        createDefault("ancient_jet_ball", throwPower = 2.5f, ancient = true)
        createDefault("ancient_origin_ball", GuaranteedModifier(), ancient = true)
        // Luxury ball effect, low priority as it must be triggered before soothe bell as of gen 4
        CobblemonEvents.FRIENDSHIP_UPDATED.subscribe(priority = Priority.LOW) { event ->
            var increment = (event.newFriendship - event.pokemon.friendship).toFloat()
            if (increment <= 0) //these affects are only meant to affect positive gains
                return@subscribe
            if (increment <= 1F) {
                event.pokemon.caughtBall.effects.filterIsInstance<FriendshipEarningBoostEffect>()
                    .forEach { increment *= it.multiplier }
            }
            event.newFriendship = event.pokemon.friendship + increment.roundToInt()
        }
    }

    override fun reload(data: Map<ResourceLocation, PokeBall>) {
        this.custom.clear()
        // ToDo once datapack pokeball is implemented load them here, we will want datapacks to be able to override our default pokeballs too, however they will never be able to disable them
    }

    override fun sync(player: ServerPlayer) {
        // ToDo once datapack pokeball is implemented sync them here
    }

    /**
     * Gets a Pokeball from registry name.
     * @return the pokeball object if found otherwise null.
     */
    @JvmStatic
    fun getPokeBall(name : ResourceLocation): PokeBall? = this.custom[name] ?: this.defaults[name]

    @JvmStatic
    fun all() = this.defaults.filterKeys { !this.custom.containsKey(it) }.values + this.custom.values

    private fun createDefault(
        name: String,
        modifier: CatchRateModifier = MultiplierModifier(1F) { _, _ -> true },
        effects: List<CaptureEffect> = emptyList(),
        waterDragValue: Float = 0.8F,
        model2d: ResourceLocation = cobblemonResource(name),
        model3d: ResourceLocation = cobblemonResource("item/${name}_model"),
        throwPower: Float = 1.25f,
        ancient: Boolean = false
    ): PokeBall {
        val identifier = cobblemonResource(name)
        //val finalModifiers = if (appendUltraBeastPenalty) modifiers + listOf(LabelModifier(0.1F, true, CobblemonPokemonLabels.ULTRA_BEAST)) else modifiers
        val pokeball = PokeBall(identifier, modifier, effects, waterDragValue, model2d, model3d, throwPower, ancient)
        this.defaults[identifier] = pokeball
        return pokeball
    }

    private fun byName(name: String): PokeBall {
        val identifier = cobblemonResource(name)
        return this.custom[identifier] ?: this.defaults[identifier]!!
    }

    /**
     * Backwards compatibility getters
     */
    @JvmName("getPOKE_BALL")
    @Deprecated("Use PokeBalls.getPokeBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getPOKE_BALL() = POKE_BALL

    @JvmName("getSLATE_BALL")
    @Deprecated("Use PokeBalls.getSlateBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getSLATE_BALL() = SLATE_BALL

    @JvmName("getAZURE_BALL")
    @Deprecated("Use PokeBalls.getAzureBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getAZURE_BALL() = AZURE_BALL

    @JvmName("getVERDANT_BALL")
    @Deprecated("Use PokeBalls.getVerdantBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getVERDANT_BALL() = VERDANT_BALL

    @JvmName("getROSEATE_BALL")
    @Deprecated("Use PokeBalls.getRoseateBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getROSEATE_BALL() = ROSEATE_BALL

    @JvmName("getCITRINE_BALL")
    @Deprecated("Use PokeBalls.getCitrineBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getCITRINE_BALL() = CITRINE_BALL

    @JvmName("getGREAT_BALL")
    @Deprecated("Use PokeBalls.getGreatBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getGREAT_BALL() = GREAT_BALL

    @JvmName("getULTRA_BALL")
    @Deprecated("Use PokeBalls.getUltraBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getULTRA_BALL() = ULTRA_BALL

    @JvmName("getMASTER_BALL")
    @Deprecated("Use PokeBalls.getMasterBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getMASTER_BALL() = MASTER_BALL

    @JvmName("getSAFARI_BALL")
    @Deprecated("Use PokeBalls.getSafariBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getSAFARI_BALL() = SAFARI_BALL

    @JvmName("getFAST_BALL")
    @Deprecated("Use PokeBalls.getFastBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getFAST_BALL() = FAST_BALL

    @JvmName("getLEVEL_BALL")
    @Deprecated("Use PokeBalls.getLevelBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getLEVEL_BALL() = LEVEL_BALL

    @JvmName("getLURE_BALL")
    @Deprecated("Use PokeBalls.getLureBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getLURE_BALL() = LURE_BALL

    @JvmName("getHEAVY_BALL")
    @Deprecated("Use PokeBalls.getHeavyBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getHEAVY_BALL() = HEAVY_BALL

    @JvmName("getLOVE_BALL")
    @Deprecated("Use PokeBalls.getLoveBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getLOVE_BALL() = LOVE_BALL

    @JvmName("getFRIEND_BALL")
    @Deprecated("Use PokeBalls.getFriendBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getFRIEND_BALL() = FRIEND_BALL

    @JvmName("getMOON_BALL")
    @Deprecated("Use PokeBalls.getMoonBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getMOON_BALL() = MOON_BALL

    @JvmName("getSPORT_BALL")
    @Deprecated("Use PokeBalls.getSportBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getSPORT_BALL() = SPORT_BALL

    @JvmName("getNET_BALL")
    @Deprecated("Use PokeBalls.getNetBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getNET_BALL() = NET_BALL

    @JvmName("getNEST_BALL")
    @Deprecated("Use PokeBalls.getNestBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getNEST_BALL() = NEST_BALL

    @JvmName("getREPEAT_BALL")
    @Deprecated("Use PokeBalls.getRepeatBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getREPEAT_BALL() = REPEAT_BALL

    @JvmName("getTIMER_BALL")
    @Deprecated("Use PokeBalls.getTimerBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getTIMER_BALL() = TIMER_BALL

    @JvmName("getLUXURY_BALL")
    @Deprecated("Use PokeBalls.getLuxuryBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getLUXURY_BALL() = LUXURY_BALL

    @JvmName("getPREMIER_BALL")
    @Deprecated("Use PokeBalls.getPremierBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getPREMIER_BALL() = PREMIER_BALL

    @JvmName("getDUSK_BALL")
    @Deprecated("Use PokeBalls.getDuskBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getDUSK_BALL() = DUSK_BALL

    @JvmName("getHEAL_BALL")
    @Deprecated("Use PokeBalls.getHealBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getHEAL_BALL() = HEAL_BALL

    @JvmName("getQUICK_BALL")
    @Deprecated("Use PokeBalls.getQuickBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getQUICK_BALL() = QUICK_BALL

    @JvmName("getCHERISH_BALL")
    @Deprecated("Use PokeBalls.getCherishBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getCHERISH_BALL() = CHERISH_BALL

    @JvmName("getPARK_BALL")
    @Deprecated("Use PokeBalls.getParkBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getPARK_BALL() = PARK_BALL

    @JvmName("getDREAM_BALL")
    @Deprecated("Use PokeBalls.getDreamBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getDREAM_BALL() = DREAM_BALL

    @JvmName("getBEAST_BALL")
    @Deprecated("Use PokeBalls.getBeastBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getBEAST_BALL() = BEAST_BALL

    @JvmName("getANCIENT_POKE_BALL")
    @Deprecated("Use PokeBalls.getAncientPokeBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_POKE_BALL() = ANCIENT_POKE_BALL

    @JvmName("getANCIENT_CITRINE_BALL")
    @Deprecated("Use PokeBalls.getAncientCitrineBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_CITRINE_BALL() = ANCIENT_CITRINE_BALL

    @JvmName("getANCIENT_VERDANT_BALL")
    @Deprecated("Use PokeBalls.getAncientVerdantBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_VERDANT_BALL() = ANCIENT_VERDANT_BALL

    @JvmName("getANCIENT_AZURE_BALL")
    @Deprecated("Use PokeBalls.getAncientAzureBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_AZURE_BALL() = ANCIENT_AZURE_BALL

    @JvmName("getANCIENT_ROSEATE_BALL")
    @Deprecated("Use PokeBalls.getAncientRoseateBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_ROSEATE_BALL() = ANCIENT_ROSEATE_BALL

    @JvmName("getANCIENT_SLATE_BALL")
    @Deprecated("Use PokeBalls.getAncientSlateBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_SLATE_BALL() = ANCIENT_SLATE_BALL

    @JvmName("getANCIENT_IVORY_BALL")
    @Deprecated("Use PokeBalls.getAncientIvoryBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_IVORY_BALL() = ANCIENT_IVORY_BALL

    @JvmName("getANCIENT_GREAT_BALL")
    @Deprecated("Use PokeBalls.getAncientGreatBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_GREAT_BALL() = ANCIENT_GREAT_BALL

    @JvmName("getANCIENT_ULTRA_BALL")
    @Deprecated("Use PokeBalls.getAncientUltraBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_ULTRA_BALL() = ANCIENT_ULTRA_BALL

    @JvmName("getANCIENT_HEAVY_BALL")
    @Deprecated("Use PokeBalls.getAncientHeavyBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_HEAVY_BALL() = ANCIENT_HEAVY_BALL

    @JvmName("getANCIENT_LEADEN_BALL")
    @Deprecated("Use PokeBalls.getAncientLeadenBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_LEADEN_BALL() = ANCIENT_LEADEN_BALL

    @JvmName("getANCIENT_GIGATON_BALL")
    @Deprecated("Use PokeBalls.getAncientGigatonBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_GIGATON_BALL() = ANCIENT_GIGATON_BALL

    @JvmName("getANCIENT_FEATHER_BALL")
    @Deprecated("Use PokeBalls.getAncientFeatherBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_FEATHER_BALL() = ANCIENT_FEATHER_BALL

    @JvmName("getANCIENT_WING_BALL")
    @Deprecated("Use PokeBalls.getAncientWingBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_WING_BALL() = ANCIENT_WING_BALL

    @JvmName("getANCIENT_JET_BALL")
    @Deprecated("Use PokeBalls.getAncientJetBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_JET_BALL() = ANCIENT_JET_BALL

    @JvmName("getANCIENT_ORIGIN_BALL")
    @Deprecated("Use PokeBalls.getAncientOriginBall(), provided for backwards compatibility until Cobblemon 1.8.")
    fun getANCIENT_ORIGIN_BALL() = ANCIENT_ORIGIN_BALL

}