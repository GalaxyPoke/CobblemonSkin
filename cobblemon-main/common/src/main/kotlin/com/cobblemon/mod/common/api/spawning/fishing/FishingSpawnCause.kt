/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.fishing

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.Cobblemon.LOGGER
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.fishing.BobberSpawnPokemonEvent
import com.cobblemon.mod.common.api.fishing.SpawnBait
import com.cobblemon.mod.common.api.fishing.SpawnBaitEffects
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.spawning.SpawnCause
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.influence.SpawnBaitInfluence
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.spawner.Spawner
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.interactive.PokerodItem
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility
import com.cobblemon.mod.common.util.cobblemonResource
import kotlin.random.Random.Default.nextInt
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack

/**
 * A spawning cause that is embellished with fishing information. Could probably also
 * have the bobber entity or something.
 *
 * @author Hiroku
 * @since February 3rd, 2024
 */
class FishingSpawnCause(
    spawner: Spawner,
    entity: Entity?,
    val rodStack: ItemStack,
    val lureLevel: Int
) : SpawnCause(spawner, entity) {
    companion object {
        const val FISHED_ASPECT = "fished"
        const val DROPS_REROLL_ASPECT = "drops_reroll"

        fun shinyReroll(pokemonEntity: PokemonEntity, effect: SpawnBait.Effect) {
            if (pokemonEntity.pokemon.shiny) return

            val shinyOdds = Cobblemon.config.shinyRate.toInt()
            if (shinyOdds <= 0) {
                return
            }
            val randomNumber = nextInt(0, shinyOdds + 1)

            if (randomNumber <= effect.value.toInt()) {
                pokemonEntity.pokemon.shiny = true
            }
        }

        fun saveDropsReroll(pokemonEntity: PokemonEntity, effect: SpawnBait.Effect) {
            pokemonEntity.pokemon.forcedAspects += DROPS_REROLL_ASPECT
        }

        fun alterNatureAttempt(pokemonEntity: PokemonEntity, effect: SpawnBait.Effect) {
            val baitStat = effect.subcategory?.let { it1 -> Stats.getStat(it1.path).identifier } ?: run {
                LOGGER.warn("One of your nature baits is missing a subcategory and failed to effect a fished Pokemon")
                return
            }
            // TIMNOTE: This replaces the static lists. It's less performant because it's being reviewed every time,
            // but also it's not something that goes off too often.
            val possibleNatures = Natures.all().filter { it.increasedStat?.identifier == baitStat }
            if (possibleNatures.isEmpty() || possibleNatures.any { it == pokemonEntity.pokemon.nature }) return
            val takenNature = possibleNatures.random()

            pokemonEntity.pokemon.nature = Natures.getNature(takenNature.name.path) ?: return
        }

        fun alterIVAttempt(pokemonEntity: PokemonEntity, effect: SpawnBait.Effect) {
            val iv = effect.subcategory ?: return

            if ((pokemonEntity.pokemon.ivs[Stats.getStat(iv.path)] ?: 0) + effect.value > 31) // if HP IV is already less than 3 away from 31
                pokemonEntity.pokemon.ivs.set(Stats.getStat(iv.path), 31)
            else
                pokemonEntity.pokemon.ivs.set(Stats.getStat(iv.path), (pokemonEntity.pokemon.ivs[Stats.getStat(iv.path)] ?: 0) + (effect.value).toInt())
        }

        fun alterGenderAttempt(pokemonEntity: PokemonEntity, effect: SpawnBait.Effect) {
            if (pokemonEntity.pokemon.species.maleRatio > 0 && pokemonEntity.pokemon.species.maleRatio < 1) // if the pokemon is allowed to be male or female
                when (effect.subcategory) {
                    cobblemonResource("male") -> if (pokemonEntity.pokemon.gender != Gender.MALE) pokemonEntity.pokemon.gender = Gender.MALE
                    cobblemonResource("female") -> if (pokemonEntity.pokemon.gender != Gender.FEMALE) pokemonEntity.pokemon.gender = Gender.FEMALE
                }
        }

        fun alterLevelAttempt(pokemonEntity: PokemonEntity, effect: SpawnBait.Effect) {
            var level = pokemonEntity.pokemon.level + effect.value.toInt()
            if (level > Cobblemon.config.maxPokemonLevel)
                level = Cobblemon.config.maxPokemonLevel
            pokemonEntity.pokemon.level = level
        }

        fun alterHAAttempt(pokemonEntity: PokemonEntity) {
            //val species = pokemonEntity.pokemon.species.let { PokemonSpecies.getByName(it.name) } ?: return
            //val ability = species.abilities.mapping[Priority.LOW]?.first()?.template?.name ?: return

            //pokemonEntity.pokemon.ability = Abilities.get(ability)?.create(false) ?: return

            // Old code from Licious that might be helpful if the above proves to not work

            // This will iterate from highest to lowest priority
            pokemonEntity.pokemon.form.abilities.mapping.values.forEach { abilities ->
                abilities.filterIsInstance<HiddenAbility>()
                    .randomOrNull ()
                    ?.let { ability ->
                        // No need to force, this is legal
                        pokemonEntity.pokemon.ability = ability.template.create(false, ability.priority)
                        return
                    }
            }
            // There was never a hidden ability :( possible but not by default
            return


        }

        fun alterFriendshipAttempt(pokemonEntity: PokemonEntity, effect: SpawnBait.Effect) {
            if (pokemonEntity.pokemon.friendship + effect.value > Cobblemon.config.maxPokemonFriendship)
                pokemonEntity.pokemon.setFriendship(Cobblemon.config.maxPokemonFriendship)
            else
                pokemonEntity.pokemon.setFriendship(pokemonEntity.pokemon.friendship + effect.value.toInt())
        }
    }

    val rodItem = rodStack.item as? PokerodItem
    // Determine the combined bait effects if any
    val baitEffects = SpawnBaitInfluence(SpawnBaitEffects.getEffectsFromRodItemStack(rodStack))

    override fun affectSpawn(action: SpawnAction<*>, entity: Entity) {
        super.affectSpawn(action, entity)
        if (entity is PokemonEntity) {
            entity.pokemon.forcedAspects += FISHED_ASPECT
            baitEffects.affectSpawn(action, entity)
            // Some of the bait actions might have changed the aspects and we need it to be
            // in the entityData IMMEDIATELY otherwise it will flash as what it would be
            // with the old aspects.
            // New aspects copy into the entity data only on the next tick.
            entity.entityData.set(PokemonEntity.ASPECTS, entity.pokemon.aspects)
            CobblemonEvents.BOBBER_SPAWN_POKEMON_MODIFY.post(
                BobberSpawnPokemonEvent.Modify(
                    spawnAction = action,
                    rod = rodStack,
                    pokemon = entity
                )
            )
        }
    }

    override fun affectWeight(detail: SpawnDetail, spawnablePosition: SpawnablePosition, weight: Float): Float {
        val weight = baitEffects.affectWeight(detail, spawnablePosition, weight)
        return super.affectWeight(detail, spawnablePosition, weight)
    }
}