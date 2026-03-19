/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util.adapters

import com.bedrockk.molang.Expression
import com.bedrockk.molang.ast.NameExpression
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.getString
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.mojang.datafixers.util.Either
import java.lang.reflect.Type

/**
 * Deserializes what is either an Expression or an object describing a MoLang variable that will be put on an entity.
 *
 * @author Hiroku
 * @since December 28th, 2024
 */
object ExpressionOrEntityVariableAdapter : JsonDeserializer<Either<Expression, MoLangConfigVariable>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Either<Expression, MoLangConfigVariable> {
        return if (json.isJsonObject) {
            Either.right(context.deserialize(json, MoLangConfigVariable::class.java))
        } else {
            val expression = context.deserialize<Expression>(json, Expression::class.java)
            if (expression is NameExpression) {
                // In this case, it was PROBABLY an attempt at doing a string and it all went horribly wrong.
                // Interpret it as a string of the original expression.
                if (expression.names.size == 1 || expression.names.first() !in listOf("q", "query", "c", "context", "v", "variable", "m", "math")) {
                    return Either.left(("'" + expression.getString() + "'").asExpression())
                }
            }
            Either.left(expression)
        }
    }
}