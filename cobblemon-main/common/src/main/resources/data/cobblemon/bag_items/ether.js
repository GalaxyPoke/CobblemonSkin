/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
    /* ether MOVE AMOUNT */
    /* ether MOVE */
    use(battle, pokemon, itemId, data) {
        const moveId = data[0];
        var amount = 999;
        if (data.length > 1) {
            amount = parseInt(data[1]);
        }

        const move = pokemon.getMoveData(moveId);
        if (move) {
            var newPP = move.pp + amount;
            if (newPP > move.maxpp) {
                newPP = move.maxpp;
            }

            move.pp = newPP;

            pokemon.updatePP();
        }
    }
}