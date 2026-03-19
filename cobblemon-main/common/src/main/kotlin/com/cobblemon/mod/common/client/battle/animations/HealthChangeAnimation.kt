/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.battle.animations

import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon

class HealthChangeAnimation(private val newHealth: Float, private val duration: Float = 1F) : TileAnimation {

    private var passedSeconds = 0F
    private var initialHealthRatio = -1F
    private var ratioDifference = 0F
    private var coercedNewHealth = -1F

    override fun shouldHoldUntilNextAnimation() = false
    override fun invoke(activeBattlePokemon: ActiveClientBattlePokemon, deltaTicks: Float): Boolean {
        // We don't update the ClientBattlePokemon flat property since that's static since the start of a battle
        val pokemon = activeBattlePokemon.battlePokemon ?: return true
        if (this.coercedNewHealth == -1F) {
            this.coercedNewHealth = if (!pokemon.isHpFlat) this.newHealth.coerceAtMost(1F) else this.newHealth
        }
        if (this.initialHealthRatio == -1F) {
            this.initialHealthRatio = pokemon.hpValue
            this.ratioDifference = this.coercedNewHealth - this.initialHealthRatio
        }

        this.passedSeconds += deltaTicks / 20
        this.passedSeconds = passedSeconds.coerceAtMost(duration)
        val progress = this.passedSeconds / this.duration
        pokemon.hpValue = this.initialHealthRatio + progress * this.ratioDifference
        return this.passedSeconds == this.duration
    }

}