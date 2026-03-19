/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.pokeball

import com.cobblemon.mod.common.api.reactive.Observable
import com.cobblemon.mod.common.api.reactive.SettableObservable
import com.cobblemon.mod.common.api.scheduling.Schedulable
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.AncientPokeBallModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokeball.PokeBallModel
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import kotlin.random.Random

@Suppress("NAME_SHADOWING")
abstract class PokeBallPosableState : PosableState(), Schedulable {
    abstract val stateEmitter: SettableObservable<EmptyPokeBallEntity.CaptureState>
    abstract val shakeEmitter: Observable<Unit>
    protected open val group: String
        get() = if (this.currentModel is AncientPokeBallModel) "ancient_poke_ball" else "poke_ball"
    protected open var lastKnownState: EmptyPokeBallEntity.CaptureState? = null

    open fun initSubscriptions() {
        stateEmitter.subscribe { state ->
            when (state) {
                EmptyPokeBallEntity.CaptureState.HIT -> {
                    doLater {
                        val model = this.currentModel!!
                        after(seconds = 0.2F) {
                            if (model is PokeBallModel && stateEmitter.get() == EmptyPokeBallEntity.CaptureState.HIT) {
                                doLater latest@{
                                    model.moveToPose(this, model.poses["open"]!!)
                                    after(seconds = 1.75F) {
                                        model.moveToPose(this, model.poses["shut"]!!)
                                    }
                                }
                            }
                        }
                    }
                }

                EmptyPokeBallEntity.CaptureState.FALL -> {}
                EmptyPokeBallEntity.CaptureState.CRITICAL -> {
                    doLater {
                        setActiveAnimations(currentModel!!.bedrockStateful(group, "critical"))
                    }
                }
                EmptyPokeBallEntity.CaptureState.SHAKE -> {
                    if (this.lastKnownState != EmptyPokeBallEntity.CaptureState.CRITICAL) {
                        // Skip the bounce on a critical capture, the animation replaces this step
                        doLater {
                            setActiveAnimations(currentModel!!.bedrockStateful(group, "bounce"))
                        }
                    }
                    shakeEmitter
                        .pipe(Observable.emitWhile { stateEmitter.get() == EmptyPokeBallEntity.CaptureState.SHAKE })
                        .subscribe {
                            val bob = "bob${Random.Default.nextInt(6) + 1}"
                            doLater { setActiveAnimations(currentModel!!.bedrockStateful(group, bob)) }
                        }
                }
                EmptyPokeBallEntity.CaptureState.CAPTURED -> {
                    doLater {
                        setActiveAnimations(currentModel!!.bedrockStateful(group, "capture"))
                    }
                }
                EmptyPokeBallEntity.CaptureState.BROKEN_FREE -> {
                    doLater {
                        setActiveAnimations(currentModel!!.bedrockStateful(group, "break"))
                    }
                }
                else -> {}
            }
            this.lastKnownState = state
        }
    }
}