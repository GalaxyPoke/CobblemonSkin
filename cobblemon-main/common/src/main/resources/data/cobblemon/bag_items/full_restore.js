/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
    use(battle, pokemon, itemId, data) {
        var amount = pokemon.heal(pokemon.maxhp - pokemon.hp);
        if (amount) {
            battle.add('-heal', pokemon, pokemon.getHealth, '[from] bagitem: ' + itemId);
        }
        pokemon.cureStatus(true);
        pokemon.removeVolatile('confusion');
    }
}