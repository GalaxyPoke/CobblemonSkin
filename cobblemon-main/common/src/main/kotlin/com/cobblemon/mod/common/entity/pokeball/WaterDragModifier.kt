/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.pokeball

/**
 * A Cobblemon extension to make it possible to add water drag values to our Pokeballs without relying on the PersistentProjectileEntity implementation.
 */
interface WaterDragModifier {

    /**
     * Calculates and returns the value of the water drag.
     *
     * @return The value of the water drag on the entity.
     */
    fun waterDrag(): Float

}