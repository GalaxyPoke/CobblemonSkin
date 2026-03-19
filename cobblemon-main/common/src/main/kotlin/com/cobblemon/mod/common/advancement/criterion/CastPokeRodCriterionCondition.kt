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

class CastPokeRodContext(val baitId: ResourceLocation)

class CastPokeRodCriterionCondition(
    playerCtx: Optional<ContextAwarePredicate>,
    val baitId: String
): SimpleCriterionCondition<CastPokeRodContext>(playerCtx) {

    companion object {
        val CODEC: Codec<CastPokeRodCriterionCondition> = RecordCodecBuilder.create { it.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(CastPokeRodCriterionCondition::playerCtx),
                Codec.STRING.optionalFieldOf("baitId", "empty_bait").forGetter(CastPokeRodCriterionCondition::baitId)
        ).apply(it, { playerCtx, baitId -> CastPokeRodCriterionCondition(playerCtx, baitId.ifEmpty { "empty_bait" }) }) }
    }

    override fun matches(player: ServerPlayer, context: CastPokeRodContext): Boolean {
        return (context.baitId == this.baitId.asIdentifierDefaultingNamespace() || this.baitId == "empty_bait")
    }
}
