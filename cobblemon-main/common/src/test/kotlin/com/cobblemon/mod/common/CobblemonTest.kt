/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.util.asExpression
import com.cobblemon.mod.common.util.asExpressionLike
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CobblemonTest {
    // Just use this test to quickly check your molang for issues
    @Test
    fun `should load this molang that is bothering me`() {
        val s = """
            q.entity.has_memory_value('cobblemon:hive_location') ? {
                t.position = q.entity.get_position_memory('cobblemon:hive_location');
                t.wander_control = q.entity.get_wander_control_memory();
                t.wander_control.set_center(t.position.x, t.position.y, t.position.z, 2, 64);
            };
        """.trimIndent()

        val expr = s.asExpressionLike()

        assertNotNull(expr)
    }
}