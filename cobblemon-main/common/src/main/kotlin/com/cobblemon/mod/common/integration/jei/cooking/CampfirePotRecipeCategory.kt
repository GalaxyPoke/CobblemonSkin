/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.integration.jei.cooking

import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.berry.Berries
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.CRAFTING_GRID_WIDTH
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.SEASONING_SLOTS
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotScreen.Companion.COOK_PROGRESS_HEIGHT
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotScreen.Companion.COOK_PROGRESS_SPRITE
import com.cobblemon.mod.common.client.gui.cookingpot.CookingPotScreen.Companion.COOK_PROGRESS_WIDTH
import com.cobblemon.mod.common.item.crafting.CookingPotRecipeBase
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.drawable.IDrawableAnimated
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.recipe.category.IRecipeCategory
import mezz.jei.api.registration.IRecipeCategoryRegistration
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient

class CampfirePotRecipeCategory(registration: IRecipeCategoryRegistration) : IRecipeCategory<CookingPotRecipeBase> {

    companion object {
        val RECIPE_TYPE = RecipeType.create("cobblemon", "campfire_pot_recipe", CookingPotRecipeBase::class.java)!!
        val CAMPFIRE_POT_TEXTURE: ResourceLocation = cobblemonResource("textures/gui/jei/campfire_pot.png")

        const val TEXTURE_WIDTH = 146
        const val TEXTURE_HEIGHT = 59
        const val WIDTH = 146
        const val HEIGHT = 59

        val EXAMPLE_SEASONINGS = BuiltInRegistries.ITEM.map { it.defaultInstance }.filter { Seasonings.isSeasoning(it) }
    }

    val guiHelper: IGuiHelper = registration.jeiHelpers.guiHelper
    val campfirePotBackground: IDrawable = guiHelper.drawableBuilder(CAMPFIRE_POT_TEXTURE, 0, 0, WIDTH, HEIGHT)
        .setTextureSize(TEXTURE_WIDTH, TEXTURE_HEIGHT)
        .build()
    val cookProgressSprite: IDrawableAnimated =
        guiHelper.drawableBuilder(COOK_PROGRESS_SPRITE, 0, 0, COOK_PROGRESS_WIDTH, COOK_PROGRESS_HEIGHT)
        .setTextureSize(22, COOK_PROGRESS_HEIGHT)
        .buildAnimated(CampfireBlockEntity.COOKING_TOTAL_TIME, IDrawableAnimated.StartDirection.LEFT, false)
    val campfirePotIcon: IDrawable = guiHelper.createDrawableItemStack(CobblemonItems.CAMPFIRE_POT_BLACK.defaultInstance)

    override fun getRecipeType(): RecipeType<CookingPotRecipeBase?>? = RECIPE_TYPE
    override fun getTitle(): Component? = lang("container.campfire_pot")
    override fun getBackground(): IDrawable? = campfirePotBackground
    override fun getIcon(): IDrawable? = campfirePotIcon

    override fun setRecipe(
        builder: IRecipeLayoutBuilder,
        recipe: CookingPotRecipeBase,
        focuses: IFocusGroup
    ) {
        recipe.ingredients.forEachIndexed { index, ingredient ->
            val column = index / CRAFTING_GRID_WIDTH
            val row = index % CRAFTING_GRID_WIDTH

            val x = 16 + row * 18
            val y = 1 + column * 18

            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredients(ingredient)
        }

        if (recipe.seasoningProcessors.isNotEmpty()) {
            val processedSeasonings = EXAMPLE_SEASONINGS.filter { item ->
                var valid = true
                if (!item.`is`(recipe.seasoningTag)) return@filter false
                recipe.seasoningProcessors.forEach {
                    if (!it.consumesItem(item)) {
                        valid = false
                        return@forEach
                    }
                }
                valid
            }

            for ((index, _) in SEASONING_SLOTS.withIndex()) {
                val x = 93 + index * 18
                val y = 1

                builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredients(VanillaTypes.ITEM_STACK, processedSeasonings.shuffled())
            }
        }

        val registryAccess = Minecraft.getInstance().level?.registryAccess() ?: throw IllegalStateException("Registry access not found")
        builder.addSlot(RecipeIngredientRole.OUTPUT, 111, 38).addIngredients(Ingredient.of(recipe.getResultItem(registryAccess)))
    }

    override fun draw(
        recipe: CookingPotRecipeBase,
        recipeSlotsView: IRecipeSlotsView,
        guiGraphics: GuiGraphics,
        mouseX: Double,
        mouseY: Double
    ) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY)

        cookProgressSprite.draw(guiGraphics, 79, 22)
    }
}