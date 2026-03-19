/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.cobblemon.mod.common.api.cooking.Flavour
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.util.cobblemonResource
import net.minecraft.resources.ResourceLocation

/**
 * Registry for all Nature types
 * Get or register nature types
 *
 * @author Deltric
 * @since January 13th, 2022
 */
object Natures {
    private val allNatures = mutableListOf<Nature>()

    @JvmField
    val HARDY = registerNature(
        Nature(cobblemonResource("hardy"), "cobblemon.nature.hardy",
        null, null, null, null)
    )

    @JvmField
    val LONELY = registerNature(
        Nature(cobblemonResource("lonely"), "cobblemon.nature.lonely",
        Stats.ATTACK, Stats.DEFENCE, Flavour.SPICY, Flavour.SOUR)
    )

    @JvmField
    val BRAVE = registerNature(
        Nature(cobblemonResource("brave"), "cobblemon.nature.brave",
            Stats.ATTACK, Stats.SPEED, Flavour.SPICY, Flavour.SWEET)
    )

    @JvmField
    val ADAMANT = registerNature(
        Nature(cobblemonResource("adamant"), "cobblemon.nature.adamant",
        Stats.ATTACK, Stats.SPECIAL_ATTACK, Flavour.SPICY, Flavour.DRY)
    )

    @JvmField
    val NAUGHTY = registerNature(
        Nature(cobblemonResource("naughty"), "cobblemon.nature.naughty",
        Stats.ATTACK, Stats.SPECIAL_DEFENCE, Flavour.SPICY, Flavour.BITTER)
    )

    @JvmField
    val BOLD = registerNature(
        Nature(cobblemonResource("bold"), "cobblemon.nature.bold",
        Stats.DEFENCE, Stats.ATTACK, Flavour.SOUR, Flavour.SPICY)
    )

    @JvmField
    val DOCILE = registerNature(
        Nature(cobblemonResource("docile"), "cobblemon.nature.docile",
        null, null, null, null)
    )

    @JvmField
    val RELAXED = registerNature(
        Nature(cobblemonResource("relaxed"), "cobblemon.nature.relaxed",
        Stats.DEFENCE, Stats.SPEED, Flavour.SOUR, Flavour.SWEET)
    )

    @JvmField
    val IMPISH = registerNature(
        Nature(cobblemonResource("impish"), "cobblemon.nature.impish",
        Stats.DEFENCE, Stats.SPECIAL_ATTACK, Flavour.SOUR, Flavour.DRY)
    )

    @JvmField
    val LAX = registerNature(
        Nature(cobblemonResource("lax"), "cobblemon.nature.lax",
        Stats.DEFENCE, Stats.SPECIAL_DEFENCE, Flavour.SOUR, Flavour.BITTER)
    )

    @JvmField
    val TIMID = registerNature(
        Nature(cobblemonResource("timid"), "cobblemon.nature.timid",
        Stats.SPEED, Stats.ATTACK, Flavour.SWEET, Flavour.SPICY)
    )

    @JvmField
    val HASTY = registerNature(
        Nature(cobblemonResource("hasty"), "cobblemon.nature.hasty",
        Stats.SPEED, Stats.DEFENCE, Flavour.SWEET, Flavour.SOUR)
    )

    @JvmField
    val SERIOUS = registerNature(
        Nature(cobblemonResource("serious"), "cobblemon.nature.serious",
        null, null, null, null)
    )

    @JvmField
    val JOLLY = registerNature(
        Nature(cobblemonResource("jolly"), "cobblemon.nature.jolly",
        Stats.SPEED, Stats.SPECIAL_ATTACK, Flavour.SWEET, Flavour.DRY)
    )

    @JvmField
    val NAIVE = registerNature(
        Nature(cobblemonResource("naive"), "cobblemon.nature.naive",
        Stats.SPEED, Stats.SPECIAL_DEFENCE, Flavour.SWEET, Flavour.BITTER)
    )

    @JvmField
    val MODEST = registerNature(
        Nature(cobblemonResource("modest"), "cobblemon.nature.modest",
        Stats.SPECIAL_ATTACK, Stats.ATTACK, null, null)
    )

    @JvmField
    val MILD = registerNature(
        Nature(cobblemonResource("mild"), "cobblemon.nature.mild",
        Stats.SPECIAL_ATTACK, Stats.DEFENCE, Flavour.DRY, Flavour.SOUR)
    )

    @JvmField
    val QUIET = registerNature(
        Nature(cobblemonResource("quiet"), "cobblemon.nature.quiet",
        Stats.SPECIAL_ATTACK, Stats.SPEED, Flavour.DRY, Flavour.SWEET)
    )

    @JvmField
    val BASHFUL = registerNature(
        Nature(cobblemonResource("bashful"), "cobblemon.nature.bashful",
        null, null, null, null)
    )

    @JvmField
    val RASH = registerNature(
        Nature(cobblemonResource("rash"), "cobblemon.nature.rash",
        Stats.SPECIAL_ATTACK, Stats.SPECIAL_DEFENCE, Flavour.DRY, Flavour.BITTER)
    )

    @JvmField
    val CALM = registerNature(
        Nature(cobblemonResource("calm"), "cobblemon.nature.calm",
        Stats.SPECIAL_DEFENCE, Stats.ATTACK, Flavour.BITTER, Flavour.SPICY)
    )

    @JvmField
    val GENTLE = registerNature(
        Nature(cobblemonResource("gentle"), "cobblemon.nature.gentle",
        Stats.SPECIAL_DEFENCE, Stats.DEFENCE, Flavour.BITTER, Flavour.SOUR)
    )

    @JvmField
    val SASSY = registerNature(
        Nature(cobblemonResource("sassy"), "cobblemon.nature.sassy",
        Stats.SPECIAL_DEFENCE, Stats.SPEED, Flavour.BITTER, Flavour.SWEET)
    )

    @JvmField
    val CAREFUL = registerNature(
        Nature(cobblemonResource("careful"), "cobblemon.nature.careful",
        Stats.SPECIAL_DEFENCE, Stats.SPECIAL_ATTACK, Flavour.BITTER, Flavour.DRY)
    )

    @JvmField
    val QUIRKY = registerNature(
        Nature(cobblemonResource("quirky"), "cobblemon.nature.quirky",
        null, null, null, null)
    )


    /**
     * Registers a new nature type
     */
    @JvmStatic
    fun registerNature(nature: Nature): Nature {
        allNatures.add(nature)
        return nature
    }

    /**
     * Gets a nature by registry name
     * @return a nature type or null
     */
    @JvmStatic
    fun getNature(name: ResourceLocation): Nature? {
        return allNatures.find { nature -> nature.name == name }
    }

    /**
     * Utility method to get a nature by string
     * @return a nature type or null
     */
    @JvmStatic
    fun getNature(identifier: String) = getNature(cobblemonResource(identifier))
            ?: getNature(ResourceLocation.parse(identifier))

    /**
     * Helper function for a random Nature
     * @return a random nature type
     */
    @JvmStatic
    fun getRandomNature(): Nature {
        return allNatures.random()
    }

    @JvmStatic
    fun all(): Collection<Nature> = this.allNatures.toList()

    /**
     * Backwards compatibility getters
     */
    @JvmName("getHARDY")
    @Deprecated("Use Natures.HARDY, provided for backwards compatibility until Cobblemon 1.8.")
    fun getHARDY() = HARDY

    @JvmName("getLONELY")
    @Deprecated("Use Natures.LONELY, provided for backwards compatibility until Cobblemon 1.8.")
    fun getLONELY() = LONELY

    @JvmName("getBRAVE")
    @Deprecated("Use Natures.BRAVE, provided for backwards compatibility until Cobblemon 1.8.")
    fun getBRAVE() = BRAVE

    @JvmName("getADAMANT")
    @Deprecated("Use Natures.ADAMANT, provided for backwards compatibility until Cobblemon 1.8.")
    fun getADAMANT() = ADAMANT

    @JvmName("getNAUGHTY")
    @Deprecated("Use Natures.NAUGHTY, provided for backwards compatibility until Cobblemon 1.8.")
    fun getNAUGHTY() = NAUGHTY

    @JvmName("getBOLD")
    @Deprecated("Use Natures.BOLD, provided for backwards compatibility until Cobblemon 1.8.")
    fun getBOLD() = BOLD

    @JvmName("getDOCILE")
    @Deprecated("Use Natures.DOCILE, provided for backwards compatibility until Cobblemon 1.8.")
    fun getDOCILE() = DOCILE

    @JvmName("getRELAXED")
    @Deprecated("Use Natures.RELAXED, provided for backwards compatibility until Cobblemon 1.8.")
    fun getRELAXED() = RELAXED

    @JvmName("getIMPISH")
    @Deprecated("Use Natures.IMPISH, provided for backwards compatibility until Cobblemon 1.8.")
    fun getIMPISH() = IMPISH

    @JvmName("getLAX")
    @Deprecated("Use Natures.LAX, provided for backwards compatibility until Cobblemon 1.8.")
    fun getLAX() = LAX

    @JvmName("getTIMID")
    @Deprecated("Use Natures.TIMID, provided for backwards compatibility until Cobblemon 1.8.")
    fun getTIMID() = TIMID

    @JvmName("getHASTY")
    @Deprecated("Use Natures.HASTY, provided for backwards compatibility until Cobblemon 1.8.")
    fun getHASTY() = HASTY

    @JvmName("getSERIOUS")
    @Deprecated("Use Natures.SERIOUS, provided for backwards compatibility until Cobblemon 1.8.")
    fun getSERIOUS() = SERIOUS

    @JvmName("getJOLLY")
    @Deprecated("Use Natures.JOLLY, provided for backwards compatibility until Cobblemon 1.8.")
    fun getJOLLY() = JOLLY

    @JvmName("getNAIVE")
    @Deprecated("Use Natures.NAIVE, provided for backwards compatibility until Cobblemon 1.8.")
    fun getNAIVE() = NAIVE

    @JvmName("getMODEST")
    @Deprecated("Use Natures.MODEST, provided for backwards compatibility until Cobblemon 1.8.")
    fun getMODEST() = MODEST

    @JvmName("getMILD")
    @Deprecated("Use Natures.MILD, provided for backwards compatibility until Cobblemon 1.8.")
    fun getMILD() = MILD

    @JvmName("getQUIET")
    @Deprecated("Use Natures.QUIET, provided for backwards compatibility until Cobblemon 1.8.")
    fun getQUIET() = QUIET

    @JvmName("getBASHFUL")
    @Deprecated("Use Natures.BASHFUL, provided for backwards compatibility until Cobblemon 1.8.")
    fun getBASHFUL() = BASHFUL

    @JvmName("getRASH")
    @Deprecated("Use Natures.RASH, provided for backwards compatibility until Cobblemon 1.8.")
    fun getRASH() = RASH

    @JvmName("getCALM")
    @Deprecated("Use Natures.CALM, provided for backwards compatibility until Cobblemon 1.8.")
    fun getCALM() = CALM

    @JvmName("getGENTLE")
    @Deprecated("Use Natures.GENTLE, provided for backwards compatibility until Cobblemon 1.8.")
    fun getGENTLE() = GENTLE

    @JvmName("getSASSY")
    @Deprecated("Use Natures.SASSY, provided for backwards compatibility until Cobblemon 1.8.")
    fun getSASSY() = SASSY

    @JvmName("getCAREFUL")
    @Deprecated("Use Natures.CAREFUL, provided for backwards compatibility until Cobblemon 1.8.")
    fun getCAREFUL() = CAREFUL

    @JvmName("getQUIRKY")
    @Deprecated("Use Natures.QUIRKY, provided for backwards compatibility until Cobblemon 1.8.")
    fun getQUIRKY() = QUIRKY
}