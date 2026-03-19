/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.bedrockk.molang.runtime.value.MoValue
import com.cobblemon.mod.common.CobblemonCallbacks
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.platform.events.PlatformEvents
import com.cobblemon.mod.common.util.cobblemonResource

/**
 * Handles the registration of the default Cobblemon event hooks into callbacks.
 */
object CallbackHandler {
    fun setup() {
        CobblemonEvents.STARTER_CHOSEN.subscribe { CobblemonCallbacks.run(cobblemonResource("starter_chosen"), it.getContext(), it.functions) }
        CobblemonEvents.POKEMON_CAPTURED.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_captured"), it.context) }
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_entity_spawn"), mutableMapOf<String, MoValue>("pokemon_entity" to it.entity.asMoLangValue())) }
        CobblemonEvents.BATTLE_VICTORY.subscribe { CobblemonCallbacks.run(cobblemonResource("battle_victory"), it.context) }
        CobblemonEvents.POKEDEX_DATA_CHANGED_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("pokedex_data_changed_pre"), it.getContext(), it.functions) }
        CobblemonEvents.POKEDEX_DATA_CHANGED_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("pokedex_data_changed_post"), it.getContext()) }
        CobblemonEvents.FORME_CHANGE.subscribe { CobblemonCallbacks.run(cobblemonResource("forme_change"), it.context) }
        CobblemonEvents.MEGA_EVOLUTION.subscribe { CobblemonCallbacks.run(cobblemonResource("mega_evolution"), it.context) }
        CobblemonEvents.ZPOWER_USED.subscribe { CobblemonCallbacks.run(cobblemonResource("zpower_used"), it.context) }
        CobblemonEvents.TERASTALLIZATION.subscribe { CobblemonCallbacks.run(cobblemonResource("terastallization"), it.context) }
        CobblemonEvents.BATTLE_FAINTED.subscribe { CobblemonCallbacks.run(cobblemonResource("battle_fainted"), it.structContext) }
        CobblemonEvents.BATTLE_FLED.subscribe { CobblemonCallbacks.run(cobblemonResource("battle_fled"), it.context) }
        CobblemonEvents.BATTLE_STARTED_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("battle_started_pre"), it.context, it.functions) }
        CobblemonEvents.BATTLE_STARTED_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("battle_started_post"), it.context) }
        CobblemonEvents.APRICORN_HARVESTED.subscribe { CobblemonCallbacks.run(cobblemonResource("apricorn_harvested"), it.context) }
        CobblemonEvents.BERRY_HARVEST.subscribe { CobblemonCallbacks.run(cobblemonResource("berry_harvested"), it.context) }
        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe { CobblemonCallbacks.run(cobblemonResource("thrown_pokeball_hit"), emptyMap(), functions = it.functions) }
        CobblemonEvents.POKEMON_HEALED.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_healed"), it.context, it.functions) }
        CobblemonEvents.POKEMON_ASPECTS_CHANGED.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_aspects_changed"), it.context) }
        CobblemonEvents.LEVEL_UP_EVENT.subscribe { CobblemonCallbacks.run(cobblemonResource("level_up"), it.context, it.functions) }
        CobblemonEvents.FRIENDSHIP_UPDATED.subscribe { CobblemonCallbacks.run(cobblemonResource("friendship_updated"), it.context, it.functions) }
        CobblemonEvents.FULLNESS_UPDATED.subscribe { CobblemonCallbacks.run(cobblemonResource("fullness_updated"), it.context, it.functions) }
        CobblemonEvents.HYPER_TRAINED_IV_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("hyper_trained_iv_pre"), it.context, it.functions) }
        CobblemonEvents.HYPER_TRAINED_IV_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("hyper_trained_iv_post"), it.context) }
        CobblemonEvents.EV_GAINED_EVENT_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("ev_gained_pre"), it.context, it.functions) }
        CobblemonEvents.EV_GAINED_EVENT_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("ev_gained_post"), it.context) }
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("experience_gained_pre"), it.context, it.functions) }
        CobblemonEvents.EXPERIENCE_GAINED_EVENT_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("experience_gained_post"), it.context) }
        CobblemonEvents.LOOT_DROPPED.subscribe { CobblemonCallbacks.run(cobblemonResource("loot_dropped"), it.context) }
        CobblemonEvents.POKEMON_FAINTED.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_fainted"), it.context) }
        CobblemonEvents.POKEMON_GAINED.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_gained"), it.context, it.functions) }
        CobblemonEvents.POKEMON_SEEN.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_seen"), it.context, it.functions) }
        CobblemonEvents.POKEMON_SCANNED.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_scanned"), it.context) }
        CobblemonEvents.POKEMON_SENT_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_sent_pre"), it.context, it.functions) }
        CobblemonEvents.POKEMON_SENT_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_sent_post"), it.context) }
        CobblemonEvents.POKEMON_RELEASED_EVENT_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_released_pre"), it.getContext(), it.functions) }
        CobblemonEvents.POKEMON_RELEASED_EVENT_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_released_post"), it.getContext()) }
        CobblemonEvents.POKEMON_CATCH_RATE.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_catch_rate_calculated"), it.context, it.functions) }
        CobblemonEvents.POKE_BALL_CAPTURE_CALCULATED.subscribe { CobblemonCallbacks.run(cobblemonResource("poke_ball_capture_calculated"), it.context, it.functions) }
        CobblemonEvents.EVOLUTION_TESTED.subscribe { CobblemonCallbacks.run(cobblemonResource("evolution_tested"), it.context, it.functions) }
        CobblemonEvents.EVOLUTION_ACCEPTED.subscribe { CobblemonCallbacks.run(cobblemonResource("evolution_accepted"), it.context, it.functions) }
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe { CobblemonCallbacks.run(cobblemonResource("evolution_completed"), it.context) }
        CobblemonEvents.POKEMON_NICKNAMED.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_nicknamed"), it.getContext()) }
        CobblemonEvents.HELD_ITEM_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("held_item_pre"), it.getContext(), it.functions) }
        CobblemonEvents.HELD_ITEM_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("held_item_post"), it.getContext()) }
        CobblemonEvents.COSMETIC_ITEM_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("cosmetic_item_pre"), it.getContext(), it.functions) }
        CobblemonEvents.COSMETIC_ITEM_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("cosmetic_item_post"), it.getContext()) }
        CobblemonEvents.FOSSIL_REVIVED.subscribe { CobblemonCallbacks.run(cobblemonResource("fossil_revived"), it.context) }
        CobblemonEvents.BAIT_SET.subscribe { CobblemonCallbacks.run(cobblemonResource("bait_set"), it.context, it.functions) }
        CobblemonEvents.BAIT_SET_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("bait_set_pre"), it.context, it.functions) }
        CobblemonEvents.BAIT_CONSUMED.subscribe { CobblemonCallbacks.run(cobblemonResource("bait_consumed"), it.context) }
        CobblemonEvents.POKEROD_CAST_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("pokerod_cast_pre"), it.context, it.functions) }
        CobblemonEvents.POKEROD_CAST_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("pokerod_cast_post"), it.context) }
        CobblemonEvents.POKEROD_REEL.subscribe { CobblemonCallbacks.run(cobblemonResource("pokerod_reel"), it.context, it.functions) }
        CobblemonEvents.BOBBER_SPAWN_POKEMON_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("bobber_spawn_pokemon_pre"), it.context, it.functions) }
        CobblemonEvents.BOBBER_SPAWN_POKEMON_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("bobber_spawn_pokemon_post"), it.context) }
        CobblemonEvents.POKE_SNACK_SPAWN_POKEMON_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("poke_snack_spawn_pokemon_pre"), it.context, it.functions) }
        CobblemonEvents.POKE_SNACK_SPAWN_POKEMON_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("poke_snack_spawn_pokemon_post"), it.context) }
        CobblemonEvents.TRADE_EVENT_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("trade_event_pre"), it.context, it.functions) }
        CobblemonEvents.TRADE_EVENT_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("trade_event_post"), it.context) }
        CobblemonEvents.RIDE_EVENT_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("ride_event_pre"), it.context, it.functions) }
        CobblemonEvents.RIDE_EVENT_APPLY_STAMINA.subscribe { CobblemonCallbacks.run(cobblemonResource("ride_event_apply_stamina"), it.context, it.functions) }
        CobblemonEvents.RIDE_EVENT_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("ride_event_post"), it.context) }
        CobblemonEvents.WALLPAPER_UNLOCKED_EVENT.subscribe { CobblemonCallbacks.run(cobblemonResource("wallpaper_unlocked"), it.context, it.functions) }
        CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("change_pc_box_wallpaper_pre"), it.context, it.functions) }
        CobblemonEvents.CHANGE_PC_BOX_WALLPAPER_EVENT_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("change_pc_box_wallpaper"), it.context) }
        CobblemonEvents.COLLECT_EGG.subscribe { CobblemonCallbacks.run(cobblemonResource("collect_egg"), it.context, it.functions) }
        CobblemonEvents.HATCH_EGG_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("hatch_egg_pre"), it.context, it.functions) }
        CobblemonEvents.HATCH_EGG_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("hatch_egg_post"), it.context) }
        CobblemonEvents.SHOULDER_MOUNT.subscribe { CobblemonCallbacks.run(cobblemonResource("shoulder_mount"), it.context, it.functions) }
        CobblemonEvents.POKEMON_RECALL_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_recall_pre"), it.context, it.functions) }
        CobblemonEvents.POKEMON_RECALL_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("pokemon_recall_post"), it.context) }

        PlatformEvents.SERVER_PLAYER_LOGIN.subscribe(priority = Priority.LOW) { CobblemonCallbacks.run(cobblemonResource("player_logged_in"), it.context) }
        PlatformEvents.SERVER_PLAYER_LOGOUT.subscribe(priority = Priority.HIGH) { CobblemonCallbacks.run(cobblemonResource("player_logged_out"), it.context) }

        PlatformEvents.SERVER_PLAYER_TICK_PRE.subscribe { CobblemonCallbacks.run(cobblemonResource("player_tick_pre"), it.context) }
        PlatformEvents.SERVER_PLAYER_TICK_POST.subscribe { CobblemonCallbacks.run(cobblemonResource("player_tick_post"), it.context) }

        PlatformEvents.SERVER_STOPPING.subscribe { CobblemonCallbacks.run(cobblemonResource("server_stopping"), emptyMap()) }

        PlatformEvents.SERVER_PLAYER_ADVANCEMENT_EARNED.subscribe { CobblemonCallbacks.run(cobblemonResource("advancement_earned"), it.context) }
        PlatformEvents.RIGHT_CLICK_BLOCK.subscribe { CobblemonCallbacks.run(cobblemonResource("right_clicked_block"), it.context, it.functions) }
        PlatformEvents.RIGHT_CLICK_ENTITY.subscribe { CobblemonCallbacks.run(cobblemonResource("right_clicked_entity"), it.context, it.functions) }
        PlatformEvents.PLAYER_DEATH.subscribe { CobblemonCallbacks.run(cobblemonResource("player_died"), it.context) }
    }
}
