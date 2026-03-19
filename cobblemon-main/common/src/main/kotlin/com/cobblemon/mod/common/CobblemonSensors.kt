/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.entity.pokemon.ai.sensors.*
import com.cobblemon.mod.common.pokemon.ai.PokemonDisturbancesSensor
import com.cobblemon.mod.common.entity.sensor.BattlingPokemonSensor
import com.cobblemon.mod.common.entity.sensor.NPCBattlingSensor
import java.util.function.Supplier
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.sensing.Sensor
import net.minecraft.world.entity.ai.sensing.SensorType

object CobblemonSensors {
    val sensors = mutableMapOf<String, SensorType<*>>()

    @JvmField
    val NPC_BATTLING = register("npc_battling", ::NPCBattlingSensor)
    @JvmField
    val BATTLING_POKEMON = register("battling_pokemon", ::BattlingPokemonSensor)
//    val NPC_BATTLING = register("npc_battling", ::NPCBattlingSensor)
    @JvmField
    val POKEMON_DROWSY = register("pokemon_drowsy", ::DrowsySensor)

    @JvmField
    val POKEMON_ADULT = register("pokemon_adult_sensor", ::PokemonAdultSensor)

    @JvmField
    val POKEMON_DISTURBANCE = register("pokemon_disturbance_sensor", ::PokemonDisturbancesSensor)

    @JvmField
    val POKEMON_DEFEND_OWNER = register("pokemon_owner_under_attack", ::DefendOwnerSensor)

    @JvmField
    val NEARBY_GROWABLE_CROPS = register("nearby_growable_crops", ::PokemonGrowableCropSensor)

    @JvmField
    val NEARBY_BEE_HIVE = register("nearby_bee_hive", ::BeeHiveSensor)

    @JvmField
    val NEARBY_FLOWER = register("nearby_flower", ::FlowerSensor)

    @JvmField
    val NEARBY_SWEET_BERRY_BUSH = register("nearby_sweet_berry_bush", ::SweetBerryBushSensor)

    @JvmField
    val POKEMON_NEARBY_WANTED_ITEM = register("pokemon_nearby_wanted_item", ::PokemonItemSensor)

    @JvmField
    val NEARBY_SACC_LEAVES = register("nearby_sacc_leaves", ::SacLeavesSensor)

    @JvmStatic
    fun <E : Entity, U : Sensor<E>> register(id: String, supplier: Supplier<U>): SensorType<U> {
        val sensor = SensorType(supplier)
        sensors[id] = sensor
        return sensor
    }
}