/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.animation

import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.addRotation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.util.math.geometry.toDegrees
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.util.Mth

/**
 * Animation used to tilt the targeted bone of the entity by a pitch that is proportionate
 * to the direction the entity is moving. This is used to make birds and fish tilt upwards
 * or downwards based on their motion.
 *
 * @author Hiroku
 * @since August 9th, 2025
 */
class PitchTiltAnimation(
    val bone: Bone,
    val maxChangePerTick: Float = 1F,
    val minPitch: Float = -45F,
    val maxPitch: Float = 45F,
) : PoseAnimation() {
    companion object {
        const val PREVIOUS_ANGLE = "previous_pitch_tilt_angle"
        const val CORRECTED_ANGLE = "corrected_pitch_tilt_angle"
        const val PITCHED_TILT = "has_pitched_tilt"
    }

    override fun setupAnim(
        context: RenderContext,
        model: PosableModel,
        state: PosableState,
        limbSwing: Float,
        limbSwingAmount: Float,
        ageInTicks: Float,
        headYaw: Float,
        headPitch: Float,
        intensity: Float
    ) {
        val entity = context.entity ?: return
        if (entity.passengers.isNotEmpty()) {
            return
        }
        val delta = entity.deltaMovement
        val vertical = delta.y
        val horizontal = delta.horizontalDistance()
        // It should be the angle in degrees that becomes higher as the entity's motion becomes more upwards, and negative as the entity's motion becomes more downwards
        val angleOfMotion = if (horizontal == 0.0) {
            when {
                vertical > 0 -> maxPitch
                vertical < 0 -> minPitch
                else -> 0F
            }
        } else {
            Mth.atan2(vertical, horizontal).toDegrees()
        }.coerceIn(minPitch, maxPitch) * -1

        val lastAngle = state.numbers[PREVIOUS_ANGLE] ?: 0F
        val change = (angleOfMotion - lastAngle).coerceIn(-maxChangePerTick, maxChangePerTick)
        if (PITCHED_TILT !in state.renderMarkers) {
            state.numbers.remove(CORRECTED_ANGLE)
            val correctedAngle = (lastAngle + change) * intensity
            bone.addRotation(0, correctedAngle.toRadians())
            state.numbers[PREVIOUS_ANGLE] = correctedAngle
        } else {
            // It was already pitched, which means the body bone already has a rotation, so only rotate it a pinch more.
            bone.addRotation(0, change.toRadians() * intensity)
            state.numbers[PREVIOUS_ANGLE] = lastAngle + change * intensity
        }
        state.renderMarkers += PITCHED_TILT
    }
}