/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
    /* x_stat atk 2 */
    use(battle, pokemon, itemId, data) {
    console.log(data);
        var stat = data[0];
        var boosts = {};
        boosts[stat] = parseInt(data[1]);
        battle.boost(boosts, pokemon, null, { effectType: 'BagItem', name: itemId });
    }
}