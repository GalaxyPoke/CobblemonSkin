/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.spawning.rules.component

import com.bedrockk.molang.Expression
import com.bedrockk.molang.ast.BooleanExpression
import com.bedrockk.molang.runtime.MoLangRuntime
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.rules.selector.AllSpawnDetailSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.AllSpawnablePositionSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.SpawnDetailSelector
import com.cobblemon.mod.common.api.spawning.rules.selector.SpawnablePositionSelector
import com.cobblemon.mod.common.util.resolveBoolean
import com.google.gson.annotations.SerializedName

/**
 * A rule component that targets specific spawns and spawnable positions and judges whether it
 * is allowed to spawn. This runs prior to the regular SpawnDetail conditions.
 *
 * @author Hiroku
 * @since October 1st, 2023
 */
class FilterRuleComponent : SpawnRuleComponent {
    @Transient
    val runtime = MoLangRuntime().setup()

    @SerializedName("spawnDetailSelector", alternate = ["spawnSelector"])
    val spawnDetailSelector: SpawnDetailSelector = AllSpawnDetailSelector
    @SerializedName("spawnablePositionSelector", alternate = ["contextSelector"])
    val spawnablePositionSelector: SpawnablePositionSelector = AllSpawnablePositionSelector
    val allow: Expression = BooleanExpression(true)

    override fun affectSpawnable(detail: SpawnDetail, spawnablePosition: SpawnablePosition): Boolean {
        return if (spawnDetailSelector.selects(detail) && spawnablePositionSelector.selects(spawnablePosition)) {
            runtime.environment.setSimpleVariable("spawn", detail.struct)
            runtime.environment.setSimpleVariable("spawn_detail", detail.struct)
            runtime.environment.setSimpleVariable("spawnable_position", spawnablePosition.getOrSetupStruct())
            runtime.resolveBoolean(allow)
        } else {
            true
        }
    }
}