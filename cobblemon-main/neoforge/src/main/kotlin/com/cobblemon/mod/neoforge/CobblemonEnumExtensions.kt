/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge

import com.cobblemon.mod.common.CobblemonItems
import net.minecraft.client.RecipeBookCategories
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neoforged.fml.common.asm.enumextension.EnumProxy
import java.util.List
import java.util.function.Supplier


// to learn more about this, read https://docs.neoforged.net/docs/1.21.1/advanced/extensibleenums/#iextensibleenum
// this is the preferred way instead of mixining in ourselves, as it preserves mod compatibility
object CobblemonEnumExtensions {
    @JvmField
    val COOKING_POT_SEARCH = EnumProxy(RecipeBookCategories::class.java,
        Supplier<List<ItemStack>> { listOf(ItemStack(Items.COMPASS)) as List<ItemStack> })
    @JvmField
    val COOKING_POT_FOODS = EnumProxy(RecipeBookCategories::class.java,
        Supplier<List<ItemStack>> { listOf(ItemStack(CobblemonItems.LEEK_AND_POTATO_STEW)) as List<ItemStack> })
    @JvmField
    val COOKING_POT_MEDICINES = EnumProxy(RecipeBookCategories::class.java,
        Supplier<List<ItemStack>> { listOf(ItemStack(CobblemonItems.POTION)) as List<ItemStack> })
    @JvmField
    val COOKING_POT_COMPLEX_DISHES = EnumProxy(RecipeBookCategories::class.java,
        Supplier<List<ItemStack>> { listOf(ItemStack(CobblemonItems.APRIJUICE_RED)) as List<ItemStack> })
    @JvmField
    val COOKING_POT_MISC = EnumProxy(RecipeBookCategories::class.java,
        Supplier<List<ItemStack>> { listOf(ItemStack(CobblemonItems.PROTEIN)) as List<ItemStack> })
}