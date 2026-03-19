/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation

class ReelInPokemonContext(val species : ResourceLocation, val baitId: ResourceLocation)

class ReelInPokemonCriterionCondition(
        playerCtx: Optional<ContextAwarePredicate>,
        val species: String,
        val baitId: String // also allows "cobblemon:empty_bait" to indicate that no bait is allowed for this criterion to match
) : SimpleCriterionCondition<ReelInPokemonContext>(playerCtx) {

    companion object {
        val CODEC: Codec<ReelInPokemonCriterionCondition> = RecordCodecBuilder.create { it.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter { it.playerCtx },
            Codec.STRING.optionalFieldOf("species", "any").forGetter { it.species },
            Codec.STRING.optionalFieldOf("baitId", "any").forGetter { it.baitId }
        ).apply(it, { playerCtx, pokemonId, baitId -> ReelInPokemonCriterionCondition(playerCtx, pokemonId, baitId) }) }
    }

    override fun matches(player: ServerPlayer, context: ReelInPokemonContext): Boolean {
        return (context.species == this.species.asIdentifierDefaultingNamespace() || this.species == "any")
                && (context.baitId == this.baitId.asIdentifierDefaultingNamespace() || this.baitId == "any" || this.baitId == "empty_bait")
    }
}
