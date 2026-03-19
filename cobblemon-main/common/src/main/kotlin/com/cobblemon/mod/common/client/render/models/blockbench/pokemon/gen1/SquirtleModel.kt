/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1

import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.frame.HeadedFrame
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pose.CobblemonPose
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.entity.PoseType.Companion.STATIONARY_POSES
import com.cobblemon.mod.common.entity.PoseType.Companion.UI_POSES
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.isBattling
import com.cobblemon.mod.common.util.isUnderWater
import com.cobblemon.mod.common.util.isInWater
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.phys.Vec3

class SquirtleModel(root: ModelPart) : PokemonPosableModel(root), HeadedFrame {
    override val rootPart = root.registerChildWithAllChildren("squirtle")
    override val head = getPart("head_ai")


    override var portraitScale = 2.02F
    override var portraitTranslation = Vec3(-0.26, -0.21, 0.0)

    override var profileScale = 0.78F
    override var profileTranslation = Vec3(0.0, 0.57, 0.0)

    lateinit var sleep: CobblemonPose
    lateinit var standing: CobblemonPose
    lateinit var walk: CobblemonPose
    lateinit var battleidle: CobblemonPose
    lateinit var shoulderLeft: CobblemonPose
    lateinit var shoulderRight: CobblemonPose
    lateinit var floating: CobblemonPose
    lateinit var swimming: CobblemonPose
    lateinit var water_surface_idle: CobblemonPose
    lateinit var water_surface_swim: CobblemonPose

    val shoulderOffset = 5.5
    val wateroffset = -10

    override fun registerPoses() {
        animations["physical"] = "q.bedrock_primary('squirtle', 'physical', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["special"] = "q.bedrock_primary('squirtle', 'special', 'look', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["status"] = "q.bedrock_primary('squirtle', 'status', q.curve('symmetrical_wide'))".asExpressionLike()
        animations["recoil"] = "q.bedrock_stateful('squirtle', 'recoil')".asExpressionLike()
        animations["cry"] = "q.bedrock_stateful('squirtle', 'cry')".asExpressionLike()

        val faint = "q.bedrock_primary('squirtle', 'faint', q.curve('one'))".asExpressionLike()

        val blink = quirk { bedrockStateful("squirtle", "blink") }
        val quirkidle = quirk { bedrockStateful("squirtle", "quirk_ground_idle") }
        val quirkwater = quirk { bedrockStateful("squirtle", "quirk_water") }

        sleep = registerPose(
            poseType = PoseType.SLEEP,
            quirks = arrayOf(blink),
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(bedrock("squirtle", "sleep"))
        )

        standing = registerPose(
            poseName = "standing",
            poseTypes = setOf(PoseType.STAND) + UI_POSES,
            quirks = arrayOf(blink, quirkidle),
            condition = { !it.isBattling && !it.isInWater },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "ground_idle")
            )
        )

        battleidle = registerPose(
            poseType = PoseType.STAND,
            poseName = "battle_standing",
            quirks = arrayOf(blink, quirkidle),
            condition = { it.isBattling },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "battle_idle")
            )
        )

        walk = registerPose(
            poseName = "walk",
            poseTypes = setOf(PoseType.WALK),
            quirks = arrayOf(blink),
            condition = { !it.isInWater },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "ground_walk")
            )
        )

        floating = registerPose(
            poseName = "floating",
            transformTicks = 10,
            poseType = PoseType.FLOAT,
            namedAnimations = mutableMapOf("faint" to faint),
            quirks = arrayOf(blink, quirkwater),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "water_idle")
            )
        )

        swimming = registerPose(
            poseName = "swimming",
            poseType = PoseType.SWIM,
            transformTicks = 10,
            namedAnimations = mutableMapOf("faint" to faint),
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "water_swim"),
            )
        )

        water_surface_idle = registerPose(
            poseName = "surface_idle",
            poseType = PoseType.STAND,
            namedAnimations = mutableMapOf("faint" to faint),
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "surfacewater_idle"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        water_surface_swim = registerPose(
            poseName = "surface_swim",
            poseType = PoseType.WALK,
            quirks = arrayOf(blink),
            condition = { it.isInWater },
            namedAnimations = mutableMapOf("faint" to faint),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "surfacewater_swim"),
            ),
            transformedParts = arrayOf(
                rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, wateroffset)
            )
        )

        shoulderLeft = registerPose(
            poseType = PoseType.SHOULDER_LEFT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "shoulder_left")
            ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -2)
                )
        )

        shoulderRight = registerPose(
            poseType = PoseType.SHOULDER_RIGHT,
            quirks = arrayOf(blink),
            animations = arrayOf(
                singleBoneLook(),
                bedrock("squirtle", "shoulder_right")
            ),
                transformedParts = arrayOf(
                        rootPart.createTransformation().addPosition(ModelPartTransformation.Y_AXIS, -2)
                )
        )

        properties["item_display_context"] = "THIRD_PERSON_RIGHT_HAND"

    }
}