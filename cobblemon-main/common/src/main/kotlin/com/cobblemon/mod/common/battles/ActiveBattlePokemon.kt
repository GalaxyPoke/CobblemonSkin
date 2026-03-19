/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
class ActiveBattlePokemon(val actor: BattleActor, var battlePokemon: BattlePokemon? = null): Targetable {
    val battle = actor.battle
    fun getSide() = actor.getSide()
    var position: Pair<ServerLevel, Vec3>? = null

    var illusion: BattlePokemon? = null

    override fun getAllActivePokemon() = battle.activePokemon
    override fun getActorShowdownId() = actor.showdownId
    override fun getActorPokemon() = actor.activePokemon
    override fun getSidePokemon() = getSide().activePokemon
    override fun getFormat() = battle.format
    override fun isAllied(other: Targetable) = getSide() == (other as ActiveBattlePokemon).getSide()
    override fun hasPokemon() = battlePokemon != null
    fun isGone() = battlePokemon?.gone ?: true
    fun isAlive() = (battlePokemon?.health ?: 0) > 0

    fun getSendOutPosition(): Vec3? {
        val pnx = getPNX()
        val actorEntityPosList = actor.getSide().actors.mapNotNull { if (it is EntityBackedBattleActor<*>) it.initialPos else null }
        val actorEntityPos = if (actorEntityPosList.size == 1)
            actorEntityPosList[0]
        else if (actorEntityPosList.size > 1)
            actorEntityPosList.fold(Vec3(0.0, 0.0, 0.0)) { acc, vec3 -> acc.add(vec3.scale(1.0 / actorEntityPosList.size)) }
        else
            null
        val opposingActorEntityList = actor.getSide().getOppositeSide().actors.mapNotNull { if (it is EntityBackedBattleActor<*>) it.initialPos else null }
        val opposingEntityPos = if (opposingActorEntityList.size == 1)
            opposingActorEntityList[0]
        else if (opposingActorEntityList.size > 1) {
            // If multiple actors per side, avg their position
            opposingActorEntityList.fold(Vec3(0.0, 0.0, 0.0)) { acc, vec3 -> acc.add(vec3.scale(1.0 / opposingActorEntityList.size)) }
        }
        else null

        var actorOffset = actorEntityPos?.let { opposingEntityPos?.subtract(it) }
        var result = actorEntityPos
        if (actorOffset != null) {
            var widthSum = 4.0 // Leave a constant to allow double/triples alignment to be consistent
            var pokemonWidth = battlePokemon?.originalPokemon?.form?.hitbox?.width ?: 0F
            val pokemonBaseScale = battlePokemon?.originalPokemon?.form?.baseScale ?: 0F
            pokemonWidth *= pokemonBaseScale
            if (battle.format.battleType.pokemonPerSide == 1) {
                battlePokemon?.let { battlePokemon ->
                    // sum of the hitbox widths of both pokemon
                    val opposingActivePokemon = (getOppositeOpponent() as ActiveBattlePokemon)
                    val opposingPokemonWidth = opposingActivePokemon.battlePokemon?.let {
                        it.originalPokemon.form.hitbox.width * it.originalPokemon.form.baseScale
                    } ?: pokemonWidth
                    widthSum = (pokemonWidth + opposingPokemonWidth) / 2.0 // Only care about the front half of each hitbox
                }
            }

            val minDistance = 4.0 + widthSum
            val actorDistance = actorOffset.length()

            if (actorDistance < minDistance) {
                val temp = actorOffset.scale(minDistance / actorDistance) ?: actorOffset
                result = actorEntityPos?.subtract(temp.subtract(actorOffset))
                actorOffset = temp
            }
            // orthogonalVector is the sideways displacement from the battle actors
            var orthogonalVector = Vec3(actorOffset.x, 0.0, actorOffset.z).normalize()
            orthogonalVector = orthogonalVector.cross(Vec3(0.0, 1.0, 0.0))

            if (battle.format.battleType.pokemonPerSide == 1) { // Singles
                result = result?.add(actorOffset.scale(if (battle.isPvW) 0.4 else 0.3))
                battlePokemon?.let { battlePokemon ->
                    val hitbox = battlePokemon.originalPokemon.form.hitbox
                    val scale = battlePokemon.originalPokemon.form.baseScale
                    orthogonalVector = orthogonalVector.scale(-0.3 - hitbox.width * scale )
                }
            } else if (battle.format.battleType.pokemonPerSide == 2) { // Doubles/Multi
                if (battle.actors.first() != battle.actors.last()) {
                    val offsetB = if (pnx[2] == 'a') orthogonalVector.scale(-1.0) else orthogonalVector
                    result = result?.add(actorOffset.scale(0.33))
                    orthogonalVector = offsetB.scale(2.5)
                }
            } else if (battle.format.battleType.pokemonPerSide == 3) { // Triples
                if (battle.actors.first() != battle.actors.last()) {
                    result = when (pnx[2]) {
                        'a' -> {
                            orthogonalVector = orthogonalVector.scale(-3.5)
                            result?.add(actorOffset.scale(0.15))
                        }
                        'b' -> {
                            orthogonalVector = Vec3.ZERO
                            result?.add(actorOffset.scale(0.3))
                        }
                        'c' -> {
                            orthogonalVector = orthogonalVector.scale(3.5)
                            result?.add(actorOffset.scale(0.15))
                        }
                        else -> result
                    }
                }
            }
            val fallbackPos = result
            result = result?.add(orthogonalVector)
            if (actor is EntityBackedBattleActor<*> && result != null) {
                val entity = actor.entity ?: return result
                // Do a single raycast to estimate if we're trying to throw a pokemon into a wall.
                // If the collision is too close to the actor, try to find a more reasonable position.
                // The motivation here is to give setPositionSafely something more reasonable to work with.
                val collisionResult = entity.level().clip(
                        ClipContext(
                                actorEntityPos!!.add(Vec3(0.0,entity.eyeHeight.toDouble(),0.0)), // start position
                                Vec3(result.x, entity.y + entity.eyeHeight.toDouble(), result.z), // end position
                                ClipContext.Block.OUTLINE,
                                ClipContext.Fluid.NONE,
                                entity
                        )
                )
                if (collisionResult.type == HitResult.Type.BLOCK) {
                    // Collided with terrain
                    result = if (collisionResult.location.distanceToSqr(actorEntityPos) < pokemonWidth) {
                        // Fallback to in between the two actors, dropping the orthogonal vector
                        // Ideally this keeps a pokemon in between the actors in a narrow tunnel
                        fallbackPos
                    } else {
                        Vec3(collisionResult.location.x, result.y, collisionResult.location.z)
                    }
                }
            }
        }
        return result
    }
}