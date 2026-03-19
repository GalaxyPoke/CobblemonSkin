/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokedex

import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.struct.VariableStruct
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokedexDataChangedEvent
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokedex.scanner.PokedexEntityData
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.readEnumConstant
import com.cobblemon.mod.common.util.readString
import com.cobblemon.mod.common.util.writeEnumConstant
import com.cobblemon.mod.common.util.writeString
import com.google.common.collect.Sets
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.ListCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * A record of a form in the Pokédex. There are a number of tracked properties regarding the form and what
 * the dex has seen, and adding to it can be done through [encountered] and [caught].
 *
 * @author Hiroku
 * @since August 23rd, 2024
 */
class FormDexRecord {
    companion object {
        val CODEC: Codec<FormDexRecord> = RecordCodecBuilder.create { instance ->
            instance.group(
                ListCodec(Codec.STRING, 0, 3).fieldOf("genders").forGetter { it.genders.map { it.name } },
                ListCodec(Codec.STRING, 0, 2).fieldOf("seenShinyStates").forGetter { it.seenShinyStates.toList() },
                Codec.STRING.fieldOf("knowledge").forGetter { it.knowledge.name }
            ).apply(instance) { genders, seenShinyStates, knowledge ->
                FormDexRecord().also {
                    it.genders.addAll(genders.map(Gender::valueOf))
                    it.seenShinyStates.addAll(seenShinyStates)
                    it.knowledge = PokedexEntryProgress.valueOf(knowledge)
                }
            }
        }
    }

    /** The genders that the dex is aware of. */
    private val genders: MutableSet<Gender> = mutableSetOf()

    /** Shiny states could be shiny or non-shiny - on the off chance they only saw the shiny, they shouldn't know what the normal looks like. */
    private val seenShinyStates =
        mutableSetOf<String>() // consider: radiants in the future (radiants should just be a resource pack tbh)

    /** The current awareness of the form that the dex has. */
    var knowledge = PokedexEntryProgress.NONE
        private set

    private val data = VariableStruct() // Could use this for various other properties maybe, consider this a draft

    @Transient
    lateinit var speciesDexRecord: SpeciesDexRecord

    @Transient
    lateinit var formName: String

    @Transient
    lateinit var struct: QueryStruct

    fun initialize(speciesDexRecord: SpeciesDexRecord, formName: String) {
        this.speciesDexRecord = speciesDexRecord
        this.formName = formName
        struct = QueryStruct(hashMapOf())
            .addFunction("data") { data }
            .addFunction("knowledge") { StringValue(knowledge.name) }
            .addFunction("has_seen_gender") { params -> genders.contains(Gender.valueOf(params.getString(0).uppercase())) }
    }

    fun clone() = FormDexRecord().also {
        it.genders.addAll(genders)
        it.seenShinyStates.addAll(seenShinyStates)
        it.knowledge = knowledge
    }

    fun encountered(pokedexEntityData: PokedexEntityData) {
        if (wouldBeDifferent(pokedexEntityData, PokedexEntryProgress.ENCOUNTERED)) {
            addInformation(pokedexEntityData, PokedexEntryProgress.ENCOUNTERED)
        }
    }

    @Deprecated("Deprecated in favor of `obtained`", ReplaceWith("obtained(pokedexEntityData)"))
    fun caught(pokedexEntityData: PokedexEntityData) {
        obtained(pokedexEntityData)
    }

    fun obtained(pokedexEntityData: PokedexEntityData) {
        if (wouldBeDifferent(pokedexEntityData, PokedexEntryProgress.CAUGHT)) {
            addInformation(pokedexEntityData, PokedexEntryProgress.CAUGHT)
        }
    }

    fun getGenders(): Set<Gender> = genders
    fun hasSeenShinyState(shiny: Boolean): Boolean = seenShinyStates.contains(if (shiny) "shiny" else "normal")
    fun getSeenShinyStates(): Set<String> = seenShinyStates

    //Used when granting all entries in dex, should figure out better way
    fun addAllShinyStatesAndGenders() {
        val form = PokemonSpecies.getByIdentifier(speciesDexRecord.id)?.getFormByName(formName)
        genders.addAll(form?.possibleGenders ?: listOf(Gender.MALE, Gender.FEMALE))

        seenShinyStates.addAll(listOf("shiny", "normal"))
        speciesDexRecord.onFormRecordUpdated(this)
    }

    fun setKnowledgeProgress(newKnowledge: PokedexEntryProgress) {
        knowledge = newKnowledge
        speciesDexRecord.onFormRecordUpdated(this)
    }

    private fun addInformation(pokedexEntityData: PokedexEntityData, knowledge: PokedexEntryProgress) {
        (speciesDexRecord.pokedexManager as? PokedexManager)?.let { pokedexManager ->
            CobblemonEvents.POKEDEX_DATA_CHANGED_PRE.postThen(
                PokedexDataChangedEvent.Pre(
                    pokedexEntityData,
                    knowledge,
                    pokedexManager.uuid,
                    this
                ),
                ifSucceeded = {
                    genders.add(pokedexEntityData.pokemon.gender)
                    seenShinyStates.add(if (pokedexEntityData.pokemon.shiny) "shiny" else "normal")
                    if (knowledge.ordinal > this.knowledge.ordinal) {
                        this.knowledge = knowledge
                    }
                    speciesDexRecord.addInformation(pokedexEntityData, knowledge)
                    speciesDexRecord.onFormRecordUpdated(this)
                    CobblemonEvents.POKEDEX_DATA_CHANGED_POST.post(
                        PokedexDataChangedEvent.Post(
                            pokedexEntityData,
                            knowledge,
                            pokedexManager.uuid,
                            this
                        )
                    )
                }
            )
        }
    }

    fun wouldBeDifferent(pokedexEntityData: PokedexEntityData, knowledge: PokedexEntryProgress): Boolean {
        return pokedexEntityData.pokemon.gender !in genders
                || (pokedexEntityData.pokemon.shiny && "shiny" !in seenShinyStates)
                || (!pokedexEntityData.pokemon.shiny && "normal" !in seenShinyStates)
                || knowledge.ordinal > this.knowledge.ordinal
                || speciesDexRecord.wouldBeDifferent(pokedexEntityData)
    }

    fun encode(buffer: RegistryFriendlyByteBuf) {
        buffer.writeCollection(genders) { _, it -> buffer.writeEnumConstant(it) }
        buffer.writeCollection(seenShinyStates) { _, it -> buffer.writeString(it) }
        buffer.writeEnumConstant(knowledge)
    }

    fun decode(buffer: RegistryFriendlyByteBuf) {
        genders.clear()
        seenShinyStates.clear()
        genders.addAll(buffer.readCollection(Sets::newHashSetWithExpectedSize) { buffer.readEnumConstant(Gender::class.java) })
        seenShinyStates.addAll(buffer.readCollection(Sets::newHashSetWithExpectedSize) { buffer.readString() })
        knowledge = buffer.readEnumConstant(PokedexEntryProgress::class.java)
    }
}