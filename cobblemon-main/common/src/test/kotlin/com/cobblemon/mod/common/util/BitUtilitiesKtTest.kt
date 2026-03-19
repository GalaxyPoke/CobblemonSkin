/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BitUtilitiesKtTest {

    @Nested
    inner class SetTests {
        @Test
        fun `should be able to set bit 1 to true`() {
            val byte = 22.toByte()
            val result = setBitForByte(byte, 1, true)
            assertEquals(23.toByte(), result)
        }

        @Test
        fun `should be able to set bit 1 to false`() {
            val byte = 55.toByte()
            val result = setBitForByte(byte, 1, false)
            assertEquals(54.toByte(), result)
        }

        @Test
        fun `should be able to set bit 7 to true`() {
            val byte = (32 + 16 + 8 + 4 + 2 + 1).toByte()
            val result = setBitForByte(byte, 7, true)
            assertEquals(127.toByte(), result)
        }

        @Test
        fun `should be able to set bit 7 to false`() {
            val byte = (64 + 32 + 16 + 8 + 4 + 2 + 1).toByte()
            val result = setBitForByte(byte, 7, false)
            assertEquals(63.toByte(), result)
        }

        @Test
        fun `should be able to set bit 4 to true`() {
            val byte = (16 + 4 + 2 + 1).toByte()
            val result = setBitForByte(byte, 4, true)
            assertEquals(31.toByte(), result)
        }

        @Test
        fun `should be able to set bit 4 to false`() {
            val byte = (16 + 8 + 4 + 2 + 1).toByte()
            val result = setBitForByte(byte, 4, false)
            assertEquals(23.toByte(), result)
        }
    }

    @Nested
    inner class GetTests {
        @Test
        fun `should be able to get bit 1 when true`() {
            val byte = (16 + 8 + 4 + 2 + 1).toByte()
            assertTrue(getBitForByte(byte, 1))
        }

        @Test
        fun `should be able to get bit 1 when false`() {
            val byte = (16 + 8 + 4 + 2).toByte()
            assertFalse(getBitForByte(byte, 1))
        }

        @Test
        fun `should be able to get bit 7 when true`() {
            val byte = 76.toByte()
            assertTrue(getBitForByte(byte, 7))
        }

        @Test
        fun `should be able to get bit 7 when false`() {
            val byte = 57.toByte()
            assertFalse(getBitForByte(byte, 7))
        }

        @Test
        fun `should be able to get bit 4 when true`() {
            val byte = 14.toByte()
            assertTrue(getBitForByte(byte, 4))
        }

        @Test
        fun `should be able to get bit 4 when false`() {
            val byte = 7.toByte()
            assertFalse(getBitForByte(byte, 4))
        }
    }
}