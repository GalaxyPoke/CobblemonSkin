/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
    use(battle, pokemon, itemId, data) {
        const curedVolatiles = [];
        var shouldCureNonvolatile = false;

        for (const status of data) {
            if (pokemon.status == status) {
                shouldCureNonvolatile = true;
            }
            else if (pokemon.volatiles[status]) {
                curedVolatiles.push(status);
            }
        }

        if (shouldCureNonvolatile) {
            pokemon.cureStatus(true);
        }
        if (curedVolatiles.length != 0) {
            for (const volatile of curedVolatiles) {
                pokemon.removeVolatile(volatile);
            }
        }
    }
}