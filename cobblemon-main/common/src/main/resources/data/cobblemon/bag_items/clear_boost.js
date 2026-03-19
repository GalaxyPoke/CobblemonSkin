/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
    use(battle, pokemon, itemId, data) {
        const boosts = {};

        for (boost in pokemon.boosts) {
            boosts[boost] = 0;
        }

        pokemon.setBoost(boosts);

        battle.add('-clearboost', pokemon, '[from] bagitem: ' + itemId);
    }
}