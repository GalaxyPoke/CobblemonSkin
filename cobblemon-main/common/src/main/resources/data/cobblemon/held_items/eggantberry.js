/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

{
	name: "Eggant Berry",
	spritenum: 0,
	isBerry: true,
	naturalGift: {
		basePower: 80,
		type: "Normal"
	},
	onUpdate(pokemon) {
		if (pokemon.volatiles['attract']) {
			pokemon.eatItem();
		}
	},
	onEat(pokemon) {
		pokemon.removeVolatile('attract');
		this.add('-end', pokemon, 'move: Attract', '[from] item: Eggant Berry');
	},
	num: -101,
	gen: 3,
	isNonstandard: "Past"
}