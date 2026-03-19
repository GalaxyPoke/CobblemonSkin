/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.ai

import com.bedrockk.molang.Expression
import com.cobblemon.mod.common.api.npc.configuration.MoLangConfigVariable
import com.mojang.datafixers.util.Either
import kotlin.jvm.optionals.getOrNull

/**
 * The issue in a lot of the behaviour datapacking is that there are times when a property should be exposed to
 * the user and other times that you want to fix the value in place. The remedy to this is using an [Either] where
 * one side is an Expression, ergo a value that is not exposed to the user, and the other side is a fully defined
 * configuration variable.
 *
 * When users are editing JSONs they can have a simple expression, or they can declare full variable to show to the
 * user. Similarly, the classes for tasks and brain configurations can have defaults that hide from the user or
 * defaults that are properly defined variables. One example where this is useful is Wander tasks which might be reused
 * in panic activities where the walk speed is tuned up. When specifying the task in the panic activity, the value of
 * the walk speed would be set (overriding the default which is the walk_speed MoLang variable) to a separate config
 * variable that is exposed to the user. Pretty neat!
 *
 * @author Hiroku
 * @since January 4th, 2025
 */
typealias ExpressionOrEntityVariable = Either<Expression, MoLangConfigVariable>

/**
 * In the case of [ExpressionOrEntityVariable] the right side is the only one that requires visibility for a user,
 * this function is a shorthand for finding all of the values in the list that have something to show the user to
 * potentially edit.
 */
fun List<ExpressionOrEntityVariable>.asVariables() = mapNotNull { it.right().getOrNull() }


