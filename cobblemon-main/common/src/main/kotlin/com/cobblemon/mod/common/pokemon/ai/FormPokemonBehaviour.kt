/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.ai

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.google.gson.annotations.SerializedName

/**
 * Form-specific AI behaviours. Any properties that are null in here should fall back to the same
 * non-null object in the root [PokemonBehaviour].
 *
 * @author Hiroku
 * @since July 15th, 2022
 */
class FormPokemonBehaviour {
    @Transient
    lateinit var parent: PokemonBehaviour

    @SerializedName("resting")
    private val _resting: RestBehaviour? = null

    @SerializedName("moving")
    private val _moving: MoveBehaviour? = null

    @SerializedName("idle")
    private val _idle: IdleBehaviour? = null

    @SerializedName("fireImmune")
    private val _fireImmune: Boolean? = null

    @SerializedName("dampensVibrations")
    private val _dampensVibrations: Boolean? = null

    @SerializedName("entityInteract")
    private val _entityInteract: EntityBehaviour? = null

    @SerializedName("blockInteract")
    private val _blockInteract: BlockBehavior? = null

    @SerializedName("lightningHit")
    private val _lightningHit: ThunderstruckBehaviour? = null

    @SerializedName("combat")
    private val _combat: CombatBehaviour? = null

    @SerializedName("itemInteract")
    private val _itemInteract: ItemBehavior? = null

    @SerializedName("herd")
    private val _herd: HerdBehaviour? = null


    @SerializedName("characteristicRainbow")
    private val _characteristicRainbow: Boolean? = null

    val resting: RestBehaviour
        get() = _resting ?: parent.resting

    val moving: MoveBehaviour
        get() = _moving ?: parent.moving

    val idle: IdleBehaviour
        get() = _idle ?: parent.idle

    val fireImmune: Boolean
        get() = _fireImmune ?: parent.fireImmune

    val entityInteract: EntityBehaviour
        get() = _entityInteract ?: parent.entityInteract

    val lightningHit: ThunderstruckBehaviour
        get() = _lightningHit ?: parent.lightningHit

    val dampensVibrations: Boolean
        get() = _dampensVibrations ?: parent.dampensVibrations

    val blockInteract: BlockBehavior
        get() = _blockInteract ?: parent.blockInteract

    val combat: CombatBehaviour
        get() = _combat ?: parent.combat

    val herd: HerdBehaviour
        get() = _herd ?: parent.herd

    val itemInteract: ItemBehavior
        get() = _itemInteract ?: parent.itemInteract

    val characteristicRainbow: Boolean
        get() = _characteristicRainbow ?: parent.characteristicRainbow

    @Transient
    val struct = ObjectValue(this).also {
        it.addFunction("resting") { resting.struct }
        it.addFunction("moving") { moving.struct }
        it.addFunction("idle") { idle.struct }
        it.addFunction("entity_interact") { entityInteract.struct }
        it.addFunction("lightning_hit") { lightningHit.struct }
        it.addFunction("characteristic_rainbow") { DoubleValue(characteristicRainbow) }
        it.addFunction("block_interact") { blockInteract.struct }
        it.addFunction("item_interact") { itemInteract.struct }
        it.addFunction("combat") { combat.struct }
        it.addFunction("herd") { herd.struct }
    }
}