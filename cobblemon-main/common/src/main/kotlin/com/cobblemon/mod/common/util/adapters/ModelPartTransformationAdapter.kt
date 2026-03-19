/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.cobblemon.mod.common.client.render.models.blockbench.JsonModelAdapter
import com.cobblemon.mod.common.client.render.models.blockbench.createTransformation
import com.cobblemon.mod.common.client.render.models.blockbench.pose.ModelPartTransformation
import com.cobblemon.mod.common.util.asExpressionLike
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type
import net.minecraft.world.phys.Vec3

object ModelPartTransformationAdapter : JsonDeserializer<ModelPartTransformation> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ModelPartTransformation? {
        json as JsonObject
        val model = JsonModelAdapter.model!!
        val partName = json.get("part").asString
        val part = model.getPart(partName).createTransformation()
        val rotation = json.get("rotation")?.asJsonArray?.let {
            Vec3(
                it[0].asDouble,
                it[1].asDouble,
                it[2].asDouble
            )
        } ?: Vec3.ZERO
        val position = json.get("position")?.asJsonArray?.let {
            Vec3(
                it[0].asDouble,
                it[1].asDouble,
                it[2].asDouble
            )
        } ?: Vec3.ZERO
        val scale = json.get("scale")?.asJsonArray?.let {
            Vec3(
                it[0].asDouble,
                it[1].asDouble,
                it[2].asDouble
            )
        } ?: Vec3(1.0,1.0,1.0)
        val isVisible = json.get("isVisible")?.asString?.asExpressionLike()
        return part.withPosition(position.x, position.y, position.z).withRotationDegrees(rotation.x, rotation.y, rotation.z).withScale(scale.x, scale.y, scale.z).also { if (isVisible != null) it.withVisibility(isVisible) }
    }
}