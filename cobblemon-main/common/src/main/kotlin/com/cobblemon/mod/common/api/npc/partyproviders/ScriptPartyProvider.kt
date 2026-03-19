/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.npc.partyproviders

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.npc.NPCPartyProvider
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.util.asArrayValue
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.toProperties
import com.cobblemon.mod.common.util.withQueryValue
import com.google.gson.JsonElement
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

class ScriptPartyProvider : NPCPartyProvider {
    companion object {
        const val TYPE = "script"
    }

    override val type = TYPE
    override var isStatic: Boolean = true
    var script: ResourceLocation = cobblemonResource("dummy")

    override fun loadFromJSON(json: JsonElement) {
        isStatic = json.asJsonObject.get("isStatic")?.asBoolean != false
        script = json.asJsonObject.get("script").asString.asIdentifierDefaultingNamespace()
    }

    override fun provide(npc: NPCEntity, level: Int, players: List<ServerPlayer>): NPCPartyStore {
        val runtime = MoLangRuntime().setup().withQueryValue("npc", npc.struct)
        runtime.withQueryValue("level", DoubleValue(level))
        runtime.withQueryValue("players", players.asArrayValue { it.asMoLangValue() })
        if (players.size == 1) {
            // This is for the convenience, most cases will be pvn one player
            runtime.withQueryValue("player", players.first().asMoLangValue())
        }
        val npcParty = NPCPartyStore(npc)
        runtime.withQueryValue("party", npcParty.struct)

        if (!CobblemonScripts.scripts.containsKey(script)) {
            Cobblemon.LOGGER.error("Script $script not found in script registry.")
            npcParty.add("magikarp".toProperties().create())
            return npcParty
        }

        CobblemonScripts.run(identifier = script, runtime = runtime)

        return npcParty
    }
}