/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.battles.model.actor

/**
 * The type of actor it is. This is used for defining PvP, PvW, etc.
 *
 * @author Hiroku
 * @since July 1st, 2022
 */
enum class ActorType {
    WILD,
    PLAYER,
    NPC
}