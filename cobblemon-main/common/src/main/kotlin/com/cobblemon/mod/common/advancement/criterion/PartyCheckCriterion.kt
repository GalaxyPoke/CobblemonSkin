/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.advancement.criterion

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EntityPredicate
import net.minecraft.server.level.ServerPlayer
import net.minecraft.resources.ResourceLocation
import java.util.Optional

class PartyCheckCriterion(
    playerCtx: Optional<ContextAwarePredicate>,
    val party: List<ResourceLocation>,
): SimpleCriterionCondition<PlayerPartyStore>(playerCtx) {

    companion object {
        val CODEC: Codec<PartyCheckCriterion> = RecordCodecBuilder.create { it.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PartyCheckCriterion::playerCtx),
            ResourceLocation.CODEC.listOf().optionalFieldOf("party", listOf()).forGetter(PartyCheckCriterion::party)
        ).apply(it, ::PartyCheckCriterion) }
    }

    override fun matches(player: ServerPlayer, context: PlayerPartyStore): Boolean {
        val matches = mutableListOf<ResourceLocation>()
        party.forEach {
            if (it == "any".asIdentifierDefaultingNamespace("minecraft") || it == "any".asIdentifierDefaultingNamespace()) {
                matches.add(it)
            }
        }
        val partyCount = context.count()
        if (matches.containsAll(party) && party.size == partyCount && matches.size == partyCount) return true
        matches.clear()
        context.iterator().forEach {
            if (party.contains(it.species.resourceIdentifier)) {
                matches.add(it.species.resourceIdentifier)
            }
        }
        return matches.containsAll(party) && matches.size == party.size
    }
}
