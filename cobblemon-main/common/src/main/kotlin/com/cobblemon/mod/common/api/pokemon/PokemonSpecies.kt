/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.pokemon

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.abilities.AbilityPool
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.api.ai.SleepDepth
import com.cobblemon.mod.common.api.ai.config.BehaviourConfig
import com.cobblemon.mod.common.api.ai.config.task.TaskConfig
import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.data.ShowdownIdentifiable
import com.cobblemon.mod.common.api.drop.DropEntry
import com.cobblemon.mod.common.api.drop.ItemDropMethod
import com.cobblemon.mod.common.api.entity.EntityDimensionsAdapter
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.adapters.MoveTemplateAdapter
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByIdentifier
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffect
import com.cobblemon.mod.common.api.pokemon.effect.adapter.ShoulderEffectAdapter
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup
import com.cobblemon.mod.common.api.pokemon.evolution.Evolution
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroup
import com.cobblemon.mod.common.api.pokemon.experience.ExperienceGroupAdapter
import com.cobblemon.mod.common.api.pokemon.moves.Learnset
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.api.pokemon.stats.Stat
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.api.riding.behaviour.RidingBehaviourSettings
import com.cobblemon.mod.common.api.riding.sound.RideSoundSettingsList
import com.cobblemon.mod.common.api.spawning.TimeRange
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.adapters.ElementalTypeAdapter
import com.cobblemon.mod.common.net.messages.client.data.SpeciesRegistrySyncPacket
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.SpeciesAdditions
import com.cobblemon.mod.common.pokemon.adapters.CobblemonRequirementAdapter
import com.cobblemon.mod.common.pokemon.ai.ObtainableItemCondition
import com.cobblemon.mod.common.pokemon.ai.ObtainableItemConditionAdapter
import com.cobblemon.mod.common.pokemon.evolution.adapters.CobblemonEvolutionAdapter
import com.cobblemon.mod.common.pokemon.evolution.adapters.CobblemonPreEvolutionAdapter
import com.cobblemon.mod.common.pokemon.evolution.adapters.LegacyItemConditionWrapperAdapter
import com.cobblemon.mod.common.util.adapters.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.common.collect.HashBasedTable
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mojang.datafixers.util.Either
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.item.Item
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.AABB

object PokemonSpecies : JsonDataRegistry<Species> {

    override val id = cobblemonResource("species")
    override val type = PackType.SERVER_DATA

    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Stat::class.java, Cobblemon.statProvider.typeAdapter)
        .registerTypeAdapter(MemoryModuleType::class.java, MemoryModuleTypeAdapter)
        .registerTypeAdapter(SensorType::class.java, SensorTypeAdapter)
        .registerTypeAdapter(BehaviourConfig::class.java, BehaviourConfigAdapter)
        .registerTypeAdapter(TaskConfig::class.java, TaskConfigAdapter)
        .registerTypeAdapter(
            TypeToken.getParameterized(Either::class.java, Expression::class.java, MoLangConfigVariable::class.java).type,
            ExpressionOrEntityVariableAdapter
        )
        .registerTypeAdapter(Activity::class.java, ActivityAdapter)
        .registerTypeAdapter(Component::class.java, TranslatedTextAdapter)
        .registerTypeAdapter(ElementalType::class.java, ElementalTypeAdapter)
        .registerTypeAdapter(AbilityTemplate::class.java, AbilityTemplateAdapter)
        .registerTypeAdapter(ShoulderEffect::class.java, ShoulderEffectAdapter)
        .registerTypeAdapter(MoveTemplate::class.java, MoveTemplateAdapter)
        .registerTypeAdapter(ExperienceGroup::class.java, ExperienceGroupAdapter)
        .registerTypeAdapter(EntityDimensions::class.java, EntityDimensionsAdapter)
        .registerTypeAdapter(Learnset::class.java, LearnsetAdapter)
        .registerTypeAdapter(Evolution::class.java, CobblemonEvolutionAdapter)
        .registerTypeAdapter(AABB::class.java, BoxAdapter)
        .registerTypeAdapter(AbilityPool::class.java, AbilityPoolAdapter)
        .registerTypeAdapter(Requirement::class.java, CobblemonRequirementAdapter)
        .registerTypeAdapter(PreEvolution::class.java, CobblemonPreEvolutionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(Set::class.java, Evolution::class.java).type, LazySetAdapter(Evolution::class))
        .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(TimeRange::class.java, IntRangesAdapter(TimeRange.timeRanges) { TimeRange(*it) })
        .registerTypeAdapter(ItemDropMethod::class.java, ItemDropMethod.adapter)
        .registerTypeAdapter(SleepDepth::class.java, SleepDepth.adapter)
        .registerTypeAdapter(DropEntry::class.java, DropEntryAdapter)
        .registerTypeAdapter(CompoundTag::class.java, NbtCompoundAdapter)
        .registerTypeAdapter(Expression::class.java, ExpressionAdapter)
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .registerTypeAdapter(Component::class.java, TextAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Biome::class.java).type, BiomeLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Block::class.java).type, BlockLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Item::class.java).type, ItemLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Structure::class.java).type, StructureLikeConditionAdapter)
        .registerTypeAdapter(TypeToken.getParameterized(RegistryLikeCondition::class.java, Fluid::class.java).type, FluidLikeConditionAdapter)
        .registerTypeAdapter(EggGroup::class.java, EggGroupAdapter)
        .registerTypeAdapter(MobEffect::class.java, RegistryElementAdapter<MobEffect>(BuiltInRegistries::MOB_EFFECT))
        .registerTypeAdapter(ItemPredicate::class.java, LegacyItemConditionWrapperAdapter)
        .registerTypeAdapter(RidingBehaviourSettings::class.java, RidingBehaviourSettingsAdapter)
        .registerTypeAdapter(RideSoundSettingsList::class.java, RideSoundSettingsListAdapter)
        .registerTypeAdapter(ObtainableItemCondition::class.java, ObtainableItemConditionAdapter)
        .disableHtmlEscaping()
        .enableComplexMapKeySerialization()
        .create()

    override val typeToken: TypeToken<Species> = TypeToken.get(Species::class.java)
    override val resourcePath = "species"

    override val observable = SimpleObservable<PokemonSpecies>()

    private val speciesByIdentifier = hashMapOf<ResourceLocation, Species>()
    private val speciesByDex = HashBasedTable.create<String, Int, Species>()

    @JvmStatic
    val species: Collection<Species>
        get() = this.speciesByIdentifier.values
    @JvmStatic
    val implemented: List<Species>
        get() = this.species.filter { it.implemented }

    init {
        SpeciesAdditions.observable.subscribe {
            this.species.forEach(Species::initialize)
            this.species.forEach(Species::resolveEvolutionMoves)
            Cobblemon.showdownThread.queue {
                it.resetRegistryData("species")
                it.sendRegistryData(allShowdownSpecies(), "species")
                it.indicateSpeciesInitialized()
                Cobblemon.LOGGER.info("Loaded {} Pokémon species", this.speciesByIdentifier.size)
                this.observable.emit(this)
            }
        }
    }

    /**
     * Finds a species by the pathname of their [ResourceLocation].
     * This method exists for the convenience of finding Cobble default Pokémon.
     * This uses [getByIdentifier] using the [Cobblemon.MODID] as the namespace and the [name] as the path.
     *
     * @param name The path of the species asset.
     * @return The [Species] if existing.
     */
    @JvmStatic
    fun getByName(name: String) = this.getByIdentifier(cobblemonResource(name))

    /**
     * Finds a [Species] by its national Pokédex entry number.
     *
     * @param ndex The [Species.nationalPokedexNumber].
     * @return The [Species] if existing.
     */
    @JvmStatic
    fun getByPokedexNumber(ndex: Int, namespace: String = Cobblemon.MODID) = this.speciesByDex.get(namespace, ndex)

    /**
     * Finds a [Species] by its unique [ResourceLocation].
     *
     * @param identifier The unique [Species.resourceIdentifier] of the [Species].
     * @return The [Species] if existing.
     */
    @JvmStatic
    fun getByIdentifier(identifier: ResourceLocation) = this.speciesByIdentifier[identifier]

    /**
     * Counts the currently loaded species.
     *
     * @return The loaded species amount.
     */
    @JvmStatic
    fun count() = this.speciesByIdentifier.size

    /**
     * Gets a map of dex numbers to species.
     *
     * @return The dex numbers map to species.
     */
    @JvmStatic
    fun getSpeciesInNamespace(namespace: String = Cobblemon.MODID): MutableMap<Int, Species> = speciesByDex.row(namespace)

    /**
     * Get a list of loaded namespaces.
     *
     * @return The list of loaded namespaces.
     */
    @JvmStatic
    fun getNamespaces() = speciesByDex.rowKeySet().toList()

    /**
     * Picks a random [Species].
     *
     * @throws [NoSuchElementException] if there are no Pokémon species loaded.
     *
     * @return A randomly selected [Species].
     */
    @JvmStatic
    fun random(): Species = this.implemented.random()

    override fun reload(data: Map<ResourceLocation, Species>) {
        this.speciesByIdentifier.clear()
        this.speciesByDex.clear()
        data.forEach { (identifier, species) ->
            species.resourceIdentifier = identifier
            this.speciesByIdentifier.put(identifier, species)?.let { old ->
                this.speciesByDex.remove(old.resourceIdentifier.namespace, old.nationalPokedexNumber)
            }
            this.speciesByDex.put(species.resourceIdentifier.namespace, species.nationalPokedexNumber, species)
        }
    }

    override fun sync(player: ServerPlayer) {
        SpeciesRegistrySyncPacket(species.toList()).sendToPlayer(player)
    }

    /**
     * The representation of a [Species] and/or [FormData] in the context of showdown.
     * This is intended as a sort of DTO that can be easily converted between JSON and Java/JS objects.
     *
     * @param species The [Species] being converted or the base species if the form is not null.
     * @param form The [FormData] being converted int o species (Showdown considers them species) will be null when dealing with the base form.
     */
    @Suppress("unused")
    internal class ShowdownSpecies(species: Species, form: FormData?) {
        val num = species.nationalPokedexNumber
        val name = if (form != null) "${createShowdownName(species)}-${form.name}" else createShowdownName(species)
        val baseSpecies = if (form != null) createShowdownName(species) else this.name
        val forme = form?.name
        // ToDo baseForme
        val otherFormes = if (form == null && species.forms.isNotEmpty()) species.forms.map { "${this.name}-${it.name}" } else emptyList()
        val formeOrder = if (form == null && this.otherFormes.isNotEmpty()) arrayListOf(this.name, *this.otherFormes.toTypedArray()) else emptyList()
        val abilities: Map<String, String> = mapOf(
            "0" to "No Ability",
            "1" to "No Ability",
            "H" to "No Ability",
            "S" to "No Ability"
        )
        val types = (form?.types ?: species.types).map { it.name }
        val preevo: String? = (form?.preEvolution ?: species.preEvolution)?.let { if (it.form == it.species.standardForm) createShowdownName(it.species) else "${createShowdownName(it.species)}-${it.form.name}" }
        // For the context of battles the content here doesn't matter whatsoever and due to how PokemonProperties work we can't guarantee an actual specific species is defined.
        val evos = if ((form?.evolutions ?: species.evolutions).isEmpty()) emptyList() else arrayListOf("")
        val nfe = this.evos.isNotEmpty()
        val eggGroups = (form?.eggGroups ?: species.eggGroups).map { it.showdownID }
        val gender: String? = when (form?.maleRatio ?: species.maleRatio) {
            0F -> "F"
            1F -> "M"
            -1F, 1.125F -> "N"
            else -> null
        }
        val genderRatio = if (this.gender == null)
            mapOf(
                "maleRatio" to (form?.maleRatio ?: species.maleRatio),
                "femaleRation" to (1F - (form?.maleRatio ?: species.maleRatio))
            ) else null
        val baseStats = mapOf(
            "hp" to (form?.baseStats?.get(Stats.HP) ?: species.baseStats[Stats.HP] ?: 1),
            "atk" to (form?.baseStats?.get(Stats.ATTACK) ?: species.baseStats[Stats.ATTACK] ?: 1),
            "def" to (form?.baseStats?.get(Stats.DEFENCE) ?: species.baseStats[Stats.DEFENCE] ?: 1),
            "spa" to (form?.baseStats?.get(Stats.SPECIAL_ATTACK) ?: species.baseStats[Stats.SPECIAL_ATTACK] ?: 1),
            "spd" to (form?.baseStats?.get(Stats.SPECIAL_DEFENCE) ?: species.baseStats[Stats.SPECIAL_DEFENCE] ?: 1),
            "spe" to (form?.baseStats?.get(Stats.SPEED) ?: species.baseStats[Stats.SPEED] ?: 1)
        )
        val heightm = (form?.height ?: species.height) / 10
        val weightkg = (form?.weight ?: species.weight) / 10
        // This is ugly, but we already have it hardcoded in the mod anyway
        val maxHP = if (species.showdownId() == "shedinja") 1 else null
        val canGigantamax: String? = if (form?.gigantamaxMove != null) form.gigantamaxMove.name else null
        val cannotDynamax = form?.dynamaxBlocked ?: species.dynamaxBlocked
        // ToDo battleOnly
        // ToDo changesFrom
        val requiredMove = form?.requiredMove
        val requiredItem = form?.requiredItem
        val requiredItems = form?.requiredItems
    }

    private fun createShowdownName(species: Species): String {
        if (species.resourceIdentifier.namespace == Cobblemon.MODID) {
            return species.name
        }
        return "${species.resourceIdentifier.namespace}:${species.name}"
    }

    internal fun allShowdownSpecies(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        this.species.forEach {species ->
            val baseSpecies = ShowdownSpecies(species, null)
            result[ShowdownIdentifiable.REGEX.replace(baseSpecies.name.lowercase(), "")] = this.gson.toJson(baseSpecies)
            species.forms.forEach { form ->
                if (form != species.standardForm) {
                    val formSpecies = ShowdownSpecies(species, form)
                    result[ShowdownIdentifiable.REGEX.replace(formSpecies.name.lowercase(), "")] = this.gson.toJson(formSpecies)
                }
            }
        }
        return result
    }
}