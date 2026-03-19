/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.battles.ai.strongBattleAI

import com.cobblemon.mod.common.api.abilities.Ability
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.pokemon.status.Statuses
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes


/**
 * Everything in this class will be eventually getting replaced by a dynamic datadriven approach
 * when moves/abilities/types become data-packable or at least queryable from showdown
 */
object AIUtility {
    fun getDamageMultiplier(attackerType: ElementalType, defenderType: ElementalType): Double {
        return typeEffectiveness[attackerType]?.get(defenderType) ?: 1.0

    }

    val typeEffectiveness: Map<ElementalType, Map<ElementalType, Double>> = mapOf(
        ElementalTypes.NORMAL to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 0.5, ElementalTypes.GHOST to 0.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.FIRE to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 0.5, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 2.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 2.0, ElementalTypes.ROCK to 0.5, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 0.5,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 2.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.WATER to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 2.0, ElementalTypes.WATER to 0.5, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 0.5,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 2.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 0.5,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 1.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.ELECTRIC to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 2.0, ElementalTypes.ELECTRIC to 0.5, ElementalTypes.GRASS to 0.5,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 0.0, ElementalTypes.FLYING to 2.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 0.5,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 1.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.GRASS to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 2.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 0.5,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 2.0, ElementalTypes.FLYING to 0.5,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 0.5, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 0.5,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.ICE to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 0.5, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 0.5, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 2.0, ElementalTypes.FLYING to 2.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 2.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.FIGHTING to mapOf(
            ElementalTypes.NORMAL to 2.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 2.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 0.5,
            ElementalTypes.PSYCHIC to 0.5, ElementalTypes.BUG to 0.5, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 0.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 2.0, ElementalTypes.STEEL to 2.0, ElementalTypes.FAIRY to 0.5
        ),
        ElementalTypes.POISON to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 0.5, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 0.5, ElementalTypes.GHOST to 0.5, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.0, ElementalTypes.FAIRY to 2.0
        ),
        ElementalTypes.GROUND to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 2.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 2.0, ElementalTypes.GRASS to 0.5,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 2.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 0.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 0.5, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 2.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.FLYING to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 0.5, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 2.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 2.0, ElementalTypes.ROCK to 0.5, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.PSYCHIC to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 2.0, ElementalTypes.POISON to 2.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 0.5, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 0.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.BUG to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 2.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 0.5, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 0.5,
            ElementalTypes.PSYCHIC to 2.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 0.5, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 2.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 0.5
        ),
        ElementalTypes.ROCK to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 2.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 2.0, ElementalTypes.FIGHTING to 0.5, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 0.5, ElementalTypes.FLYING to 2.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 2.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.GHOST to mapOf(
            ElementalTypes.NORMAL to 0.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 2.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 2.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 0.5, ElementalTypes.STEEL to 1.0, ElementalTypes.FAIRY to 1.0
        ),
        ElementalTypes.DRAGON to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 2.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 0.0
        ),
        ElementalTypes.DARK to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 1.0, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 0.5, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 2.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 2.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 0.5, ElementalTypes.STEEL to 1.0, ElementalTypes.FAIRY to 0.5
        ),
        ElementalTypes.STEEL to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 0.5, ElementalTypes.ELECTRIC to 0.5, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 2.0, ElementalTypes.FIGHTING to 1.0, ElementalTypes.POISON to 1.0, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 2.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 1.0,
            ElementalTypes.DARK to 1.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 2.0
        ),
        ElementalTypes.FAIRY to mapOf(
            ElementalTypes.NORMAL to 1.0, ElementalTypes.FIRE to 0.5, ElementalTypes.WATER to 1.0, ElementalTypes.ELECTRIC to 1.0, ElementalTypes.GRASS to 1.0,
            ElementalTypes.ICE to 1.0, ElementalTypes.FIGHTING to 2.0, ElementalTypes.POISON to 0.5, ElementalTypes.GROUND to 1.0, ElementalTypes.FLYING to 1.0,
            ElementalTypes.PSYCHIC to 1.0, ElementalTypes.BUG to 1.0, ElementalTypes.ROCK to 1.0, ElementalTypes.GHOST to 1.0, ElementalTypes.DRAGON to 2.0,
            ElementalTypes.DARK to 2.0, ElementalTypes.STEEL to 0.5, ElementalTypes.FAIRY to 1.0
        )
    )

    val multiHitMoves: Map<String, Pair<Int, Int>> = mapOf(
        // 2 - 5 hit moves
        "armthrust" to Pair(2, 5),
        "barrage" to Pair(2, 5),
        "bonerush" to Pair(2,  5),
        "bulletseed" to Pair(2, 5),
        "cometpunch" to Pair(2, 5),
        "doubleslap" to Pair(2,  5),
        "furyattack" to Pair(2,  5),
        "furyswipes" to Pair(2,  5),
        "iciclespear" to Pair(2,  5),
        "pinmissile" to Pair(2, 5),
        "rockblast" to Pair(2, 5),
        "scaleshot" to Pair(2, 5),
        "spikecannon" to Pair(2, 5),
        "tailslap" to Pair(2, 5),
        "watershuriken" to Pair(2, 5),

        // fixed hit count
        "bonemerang" to Pair(2, 2),
        "doublehit" to Pair(2, 2),
        "doubleironbash" to Pair(2, 2),
        "doublekick" to Pair(2, 2),
        "dragondarts" to Pair(2, 2),
        "dualchop" to Pair(2, 2),
        "dualwingbeat" to Pair(2, 2),
        "geargrind" to Pair(2, 2),
        "twinbeam" to Pair(2, 2),
        "twineedle" to Pair(2, 2),
        "suringstrikes" to Pair(3, 3),
        "tripledive" to Pair(3, 3),
        "watershuriken" to Pair(3, 3),

        // accuracy based multi-hit moves
        "tripleaxel" to Pair(1, 3),
        "triplekick" to Pair(1, 3),
        "populationbomb" to Pair(1, 10)
    )

    val statusMoves: Map<String, String> = mapOf(
        "willowisp" to Statuses.BURN.showdownName,
        "scald" to Statuses.BURN.showdownName,
        "scorchingsands" to Statuses.BURN.showdownName,
        "glare" to Statuses.PARALYSIS.showdownName,
        "nuzzle" to Statuses.PARALYSIS.showdownName,
        "stunspore" to Statuses.PARALYSIS.showdownName,
        "thunderwave" to Statuses.PARALYSIS.showdownName,
        "darkvoid" to Statuses.SLEEP.showdownName,
        "hypnosis" to Statuses.SLEEP.showdownName,
        "lovelykiss" to Statuses.SLEEP.showdownName,
        "relicsong" to Statuses.SLEEP.showdownName,
        "sing" to Statuses.SLEEP.showdownName,
        "sleeppower" to Statuses.SLEEP.showdownName,
        "spore" to Statuses.SLEEP.showdownName,
        "yawn" to Statuses.SLEEP.showdownName,
        "chatter" to Statuses.CONFUSE.showdownName,
        "confuseray" to Statuses.CONFUSE.showdownName,
        "dynamicpunch" to Statuses.CONFUSE.showdownName,
        "flatter" to Statuses.CONFUSE.showdownName,
        "supersonic" to Statuses.CONFUSE.showdownName,
        "swagger" to Statuses.CONFUSE.showdownName,
        "sweetkiss" to Statuses.CONFUSE.showdownName,
        "teeterdance" to Statuses.CONFUSE.showdownName,
        "poisongas" to Statuses.POISON.showdownName,
        "poisonpowder" to Statuses.POISON.showdownName,
        "toxic" to Statuses.POISON_BADLY.showdownName,
        "toxicthread" to Statuses.POISON.showdownName,
        "curse" to "cursed",
        "leechseed" to "leech"
    )

    val boostFromMoves: Map<String, Map<Stat, Int>> = mapOf(
        "bellydrum" to mapOf(Stats.ATTACK to 6),
        "bulkup" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1),
        "clangoroussoul" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1, Stats.SPEED to 1),
        "coil" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.ACCURACY to 1),
        "dragondance" to mapOf(Stats.ATTACK to 1, Stats.SPEED to 1),
        "extremeevoboost" to mapOf(Stats.ATTACK to 2, Stats.DEFENCE to 2, Stats.SPECIAL_ATTACK to 2, Stats.SPECIAL_DEFENCE to 2, Stats.SPEED to 2),
        "clangoroussoulblaze" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1, Stats.SPEED to 1),
        "filletaway" to mapOf(Stats.ATTACK to 2, Stats.SPECIAL_ATTACK to 2, Stats.SPEED to 2),
        "honeclaws" to mapOf(Stats.ATTACK to 1, Stats.ACCURACY to 1),
        "noretreat" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1, Stats.SPEED to 1),
        "shellsmash" to mapOf(Stats.ATTACK to 2, Stats.DEFENCE to -1, Stats.SPECIAL_ATTACK to 2, Stats.SPECIAL_DEFENCE to -1, Stats.SPEED to 2),
        "shiftgear" to mapOf(Stats.ATTACK to 1, Stats.SPEED to 2),
        "swordsdance" to mapOf(Stats.ATTACK to 2),
        "tidyup" to mapOf(Stats.ATTACK to 1, Stats.SPEED to 1),
        "victorydance" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPEED to 1),
        "acidarmor" to mapOf(Stats.DEFENCE to 2),
        "barrier" to mapOf(Stats.DEFENCE to 2),
        "cottonguard" to mapOf(Stats.DEFENCE to 3),
        "defensecurl" to mapOf(Stats.DEFENCE to 1),
        "irondefense" to mapOf(Stats.DEFENCE to 2),
        "shelter" to mapOf(Stats.DEFENCE to 2, Stats.EVASION to 1),
        "stockpile" to mapOf(Stats.DEFENCE to 1, Stats.SPECIAL_DEFENCE to 1),
        "stuffcheeks" to mapOf(Stats.DEFENCE to 2),
        "amnesia" to mapOf(Stats.SPECIAL_DEFENCE to 2),
        "calmmind" to mapOf(Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1),
        "geomancy" to mapOf(Stats.SPECIAL_ATTACK to 2, Stats.SPECIAL_DEFENCE to 2, Stats.SPEED to 2),
        "nastyplot" to mapOf(Stats.SPECIAL_ATTACK to 2),
        "quiverdance" to mapOf(Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1, Stats.SPEED to 1),
        "tailglow" to mapOf(Stats.SPECIAL_ATTACK to 3),
        "takeheart" to mapOf(Stats.SPECIAL_ATTACK to 1, Stats.SPECIAL_DEFENCE to 1),
        "agility" to mapOf(Stats.SPEED to 2),
        "autotomize" to mapOf(Stats.SPEED to 2),
        "rockpolish" to mapOf(Stats.SPEED to 2),
        "curse" to mapOf(Stats.ATTACK to 1, Stats.DEFENCE to 1, Stats.SPEED to -1),
        "minimize" to mapOf(Stats.EVASION to 2)
    )

    val entryHazards = listOf("spikes", "stealthrock", "stickyweb", "toxicspikes")
    val antiHazardsMoves = listOf("rapidspin", "defog", "tidyup")
    val antiBoostMoves = listOf("slearsmog","haze")
    val pivotMoves = listOf("uturn", "flipturn", "partingshot", "batonpass", "shedtail", "voltswitch", "teleport")
    val setupMoves = setOf("tailwind", "trickroom", "auroraveil", "lightscreen", "reflect")
    val selfRecoveryMoves = listOf("healorder", "milkdrink", "recover", "rest", "roost", "slackoff", "softboiled")
    val weatherSetupMoves = mapOf(
        "chillyreception" to "Snow",
        "hail" to "Hail",
        "raindance" to "RainDance",
        "sandstorm" to "Sandstorm",
        "snowscape" to "Snow",
        "sunnyday" to "SunnyDay"
    )
    val accuracyLoweringMoves = setOf("flash", "kinesis", "leaftornado", "mirrorshot", "mudbomb", "mudslap", "muddywater", "nightgaze", "octazooka", "sandattack", "secretpower", "smokescreen")
    val protectMoves = setOf("protect", "banefulbunker", "obstruct", "craftyshield", "detect", "quickguard", "spikyshield", "silktrap")

    val statusImmunityMap = mapOf(
        Statuses.BURN.showdownName to (setOf("fire") to setOf("waterbubble", "waterveil", "flareboost", "guts", "magicguard")),
        Statuses.PARALYSIS.showdownName to (setOf("electric") to setOf("limber", "guts")),
        Statuses.SLEEP.showdownName to (setOf("grass") to setOf("insomnia", "sweetveil")),
        Statuses.CONFUSE.showdownName to (setOf("fire") to setOf("owntempo", "oblivious")),
        Statuses.POISON.showdownName to (setOf("poison", "steel") to setOf("immunity", "poisonheal", "guts", "magicguard")),
        Statuses.POISON_BADLY.showdownName to (setOf("poison", "steel") to setOf("immunity", "poisonheal", "guts", "magicguard")),
        "cursed" to (setOf("ghost") to setOf("magicguard")),
        "leech" to (setOf("grass") to setOf("liquidooze", "magicguard")),
    )

    fun canAffectWithStatus(status: String, types: Iterable<ElementalType>, ability: Ability? = null): Boolean {
        val (typing, abilities) = statusImmunityMap[status] ?: return true // custom status? *shrug*

        return types.none { it.name in typing } && ability?.name !in abilities
    }

    val typeImmuneAbilities = mapOf(
        "lightingrod" to ElementalTypes.ELECTRIC,
        "flashfire" to ElementalTypes.FIRE,
        "levitate" to ElementalTypes.GROUND,
        "sapsipper" to ElementalTypes.GRASS,
        "motordrive" to ElementalTypes.ELECTRIC,
        "stormdrain" to ElementalTypes.WATER,
        "voltabsorb" to ElementalTypes.ELECTRIC,
        "waterabsorb" to ElementalTypes.WATER,
        "immunity" to ElementalTypes.POISON,
        "eartheater" to ElementalTypes.GROUND
    )
}