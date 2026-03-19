/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.behavior.BehaviorControl

/**
 * A wrapper for [BehaviorControl] that allows it to be used with any [LivingEntity]. If the class the task was
 * actually intended for is not the same as the entity being passed in, the task will just not run. This is just
 * a trick to allow type safety.
 *
 * @author Hiroku
 * @since October 19th, 2024
 */
class WrapperLivingEntityTask<T : LivingEntity>(val task: BehaviorControl<T>, val clazz: Class<T>) : BehaviorControl<LivingEntity> {
    companion object {
        inline fun <reified E : LivingEntity> BehaviorControl<E>.wrapped(): BehaviorControl<LivingEntity> {
            return WrapperLivingEntityTask(this, E::class.java)
        }
    }
    override fun tryStart(level: ServerLevel, entity: LivingEntity, gameTime: Long): Boolean {
        if (clazz.isInstance(entity)) {
            return task.tryStart(level, entity as T, gameTime)
        } else {
            return false
        }
    }

    override fun debugString() = task.debugString()
    override fun tickOrStop(level: ServerLevel, entity: LivingEntity, gameTime: Long) {
        task.tickOrStop(level, entity as T, gameTime)
    }

    override fun getStatus() = task.status
    override fun doStop(level: ServerLevel, entity: LivingEntity, gameTime: Long) = task.doStop(level, entity as T, gameTime)
}