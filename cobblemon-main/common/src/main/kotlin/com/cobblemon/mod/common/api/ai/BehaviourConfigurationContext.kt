/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.ai.config.BehaviourConfig
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.molang.MoLangFunctions.addStandardFunctions
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMostSpecificMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.molang.ObjectValue
import com.cobblemon.mod.common.api.scripting.CobblemonScripts
import com.cobblemon.mod.common.entity.MoLangScriptingEntity
import com.cobblemon.mod.common.util.activityRegistry
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.resolve
import com.cobblemon.mod.common.util.withQueryValue
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Dynamic
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.ai.sensing.SensorType
import net.minecraft.world.entity.schedule.Activity
import net.minecraft.world.entity.schedule.Schedule

class BehaviourConfigurationContext {
    val runtime = MoLangRuntime().setup()

    var appliedBehaviours: Set<ResourceLocation> = setOf()

    var defaultActivity = Activity.IDLE
    var coreActivities = setOf(Activity.CORE)
    val activities = mutableListOf<ActivityConfigurationContext>()
    val schedule = Schedule.EMPTY
    val memories = mutableSetOf<MemoryModuleType<*>>()
    val sensors = mutableSetOf<SensorType<*>>()

    val onAdd = mutableListOf<ExpressionLike>()

    fun getOrCreateActivity(activity: Activity): ActivityConfigurationContext {
        return activities.firstOrNull { it.activity == activity } ?: ActivityConfigurationContext(activity).also(activities::add)
    }

    fun addMemories(vararg memory: MemoryModuleType<*>) {
        memories.addAll(memory)
    }

    fun addMemories(memories: Collection<MemoryModuleType<*>>) {
        this.memories.addAll(memories)
    }

    fun addSensors(vararg sensor: SensorType<*>) {
        sensors.addAll(sensor)
    }

    fun addSensors(sensors: Collection<SensorType<*>>) {
        this.sensors.addAll(sensors)
    }

    fun addOnAddScript(script: ExpressionLike) {
        onAdd.add(script)
    }

    fun apply(entity: LivingEntity, behaviourConfigs: List<BehaviourConfig>, dynamic: Dynamic<*>) {
        runtime.withQueryValue("entity", entity.asMostSpecificMoLangValue())
        runtime.withQueryValue("brain", createBrainStruct(entity, this))

        if (entity is MoLangScriptingEntity) {
            entity.registerVariables(behaviourConfigs.flatMap { it.getVariables(entity, this) })
            entity.initializeScripting()
        }

        // Setup the brain config
        behaviourConfigs.forEach { it.preconfigure(entity, this) }
        behaviourConfigs.forEach { it.configure(entity, this) }

        var brain = entity.brain
        // Apply the brain config
        if (entity is MoLangScriptingEntity) {
            brain = entity.assignNewBrainWithMemoriesAndSensors(dynamic, memories, sensors)
            onAdd.forEach { runtime.resolve(it) }
        }
        activities.forEach { it.apply(entity) }
        brain.setCoreActivities(coreActivities)
        brain.setDefaultActivity(defaultActivity)
        brain.schedule = schedule
        brain.setActiveActivityIfPossible(defaultActivity)
    }

    fun createBrainStruct(entity: LivingEntity, behaviourConfigurationContext: BehaviourConfigurationContext): QueryStruct {
        return QueryStruct(hashMapOf()).addStandardFunctions()
            .addFunction("entity") { entity.asMostSpecificMoLangValue() }
            .addFunction("create_activity") { params ->
                val name = params.getString(0).asIdentifierDefaultingNamespace()
                val activity = entity.level().activityRegistry.get(name) ?: return@addFunction run {
                    Cobblemon.LOGGER.error("Tried loading activity $name as part of an entity brain but that activity does not exist")
                    DoubleValue.ZERO
                }
                val existingActivityBuilder = behaviourConfigurationContext.activities.find { it.activity == activity }
                if (existingActivityBuilder != null) {
                    return@addFunction createActivityStruct(existingActivityBuilder)
                } else {
                    val activityConfigurationContext = ActivityConfigurationContext(activity)
                    behaviourConfigurationContext.activities.add(activityConfigurationContext)
                    return@addFunction createActivityStruct(activityConfigurationContext)
                }
            }
            .addFunction("set_core_activities") { params ->
                behaviourConfigurationContext.coreActivities = params.params.map { (it as ObjectValue<ActivityConfigurationContext>).obj.activity }.toSet()
                return@addFunction DoubleValue.ONE
            }
            .addFunction("set_default_activity") { params ->
                behaviourConfigurationContext.defaultActivity = params.get<ObjectValue<ActivityConfigurationContext>>(0).obj.activity
                return@addFunction DoubleValue.ONE
            }
    }

    fun createActivityStruct(activityConfigurationContext: ActivityConfigurationContext): ObjectValue<ActivityConfigurationContext> {
        val struct = ObjectValue(activityConfigurationContext)
        struct.addStandardFunctions()
            .addFunction("add_task") { params ->
                val priority = params.getInt(0)
                val task = params.get(1) as ObjectValue<BehaviorControl<in LivingEntity>>
                activityConfigurationContext.tasks.add(Pair(priority, task.obj))
            }
        return struct
    }
}