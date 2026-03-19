/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common;

import com.cobblemon.mod.common.api.spawning.spawner.PlayerSpawner;

/**
 * Used for mixin shenanigans. ServerPlayers now store their Cobblemon spawner
 * inside, so this interface helps execute that.
 *
 * @author Hiroku
 * @since October 27th, 2025
 */
public interface PlayerSpawnerAccessor {
    PlayerSpawner getPlayerSpawner();
    void setPlayerSpawner(PlayerSpawner spawner);
}
