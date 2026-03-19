/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
    /* elixir AMOUNT */
    /* elixir */
    use(battle, pokemon, itemId, data) {
        var amount = 999;
        if (data.length > 0) {
            amount = parseInt(data[0]);
        }

        pokemon.baseMoveSlots.forEach(move => {
            var newPP = move.pp + amount;
            if (newPP > move.maxpp) {
                newPP = move.maxpp;
            }

            move.pp = newPP;
        });

        pokemon.updatePP();
    }
}