/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common


import com.cobblemon.mod.common.block.campfirepot.CookingPotMenu
import com.cobblemon.mod.common.platform.PlatformRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.inventory.MenuType

object CobblemonMenuType : PlatformRegistry<Registry<MenuType<*>>, ResourceKey<Registry<MenuType<*>>>, MenuType<*>>() {

    val COOKING_POT = MenuType.register("cooking_pot", ::CookingPotMenu)

    override val registry: Registry<MenuType<*>>
        get() = BuiltInRegistries.MENU
    override val resourceKey: ResourceKey<Registry<MenuType<*>>>
        get() = Registries.MENU
}