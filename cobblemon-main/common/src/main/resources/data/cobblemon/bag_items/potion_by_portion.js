/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
    use(battle, pokemon, itemId, data) {
        var ratio = parseFloat(data[0]);
        var causeConfusion = data[1];
        var amount = pokemon.heal(Math.floor(pokemon.maxhp * ratio));
        if (amount) {
            battle.add('-heal', pokemon, pokemon.getHealth, '[from] bagitempotion');
            if (causeConfusion) {
                pokemon.addVolatile('confusion');
            }
        }
    }
}