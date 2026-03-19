/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.cookingpot

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonNetwork.sendToServer
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.block.campfirepot.CampfirePotColor
import com.cobblemon.mod.common.block.campfirepot.CookingPotMenu
import com.cobblemon.mod.common.block.campfirepot.SeasoningSlot
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.COOKING_POT_COLOR_INDEX
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity.Companion.IS_LID_OPEN_INDEX
import com.cobblemon.mod.common.integration.jei.CobblemonJeiPlugin
import com.cobblemon.mod.common.integration.jei.cooking.CampfirePotRecipeCategory
import com.cobblemon.mod.common.item.crafting.CookingPotRecipe
import com.cobblemon.mod.common.mixin.accessor.RecipeBookComponentAccessor
import com.cobblemon.mod.common.net.messages.client.cooking.ToggleCookingPotLidPacket
import com.cobblemon.mod.common.util.cobblemonResource
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.components.StateSwitchingButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.RecipeBookMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.crafting.CraftingInput
import kotlin.math.ceil

@Environment(EnvType.CLIENT)
class CookingPotScreen(
    menu: CookingPotMenu,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<CookingPotMenu>(
    menu,
    playerInventory,
    Component.translatable("cobblemon.container.campfire_pot")
), RecipeUpdateListener {

    companion object {
        private const val BACKGROUND_HEIGHT = 166
        private const val BACKGROUND_WIDTH = 176

        const val COOK_PROGRESS_HEIGHT = 12
        const val COOK_PROGRESS_WIDTH = 22

        private val BACKGROUND = cobblemonResource("textures/gui/campfirepot/campfire_pot.png")
        val COOK_PROGRESS_SPRITE: ResourceLocation = cobblemonResource("textures/gui/campfirepot/cook_progress.png")
        private val FILTER_BUTTON_SPRITES = WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/campfire_pot_filter_enabled"),
            ResourceLocation.withDefaultNamespace("recipe_book/campfire_pot_filter_disabled"),
            ResourceLocation.withDefaultNamespace("recipe_book/campfire_pot_filter_enabled_highlighted"),
            ResourceLocation.withDefaultNamespace("recipe_book/campfire_pot_filter_disabled_highlighted")
        )
    }

    private val recipeBookComponent: RecipeBookComponent = RecipeBookComponent()
    private var widthTooNarrow: Boolean = false

    private lateinit var cookButton: CookButton

    init {
        this.imageWidth = BACKGROUND_WIDTH
        this.imageHeight = BACKGROUND_HEIGHT
    }

    override fun containerTick() {
        super.containerTick()
        this.recipeBookComponent.tick()
    }

    override fun init() {
        super.init()
        this.widthTooNarrow = this.width < 379
        this.recipeBookComponent.init(
            this.width,
            this.height,
            this.minecraft!!,
            this.widthTooNarrow,
            this.menu as RecipeBookMenu<CraftingInput, CookingPotRecipe>
        )

        if (this.recipeBookComponent.isVisible) {
            val recipeBookFilterButton = (this.recipeBookComponent as RecipeBookComponentAccessor).filterButton
            overrideRecipeBookFilterButton(recipeBookFilterButton)
        }

        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth)
        val topPos = ((height - BACKGROUND_HEIGHT) / 2)

        val recipeBookButton = ImageButton(
            this.leftPos + 5, topPos + 35, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES
        ) { button ->
            this.recipeBookComponent.toggleVisibility()
            if (this.recipeBookComponent.isVisible) overrideRecipeBookFilterButton((this.recipeBookComponent as RecipeBookComponentAccessor).filterButton)
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth)
            button.setPosition(this.leftPos + 5, topPos + 35)
        }

        this.addWidget(this.recipeBookComponent)
        this.addRenderableWidget(recipeBookButton)

        this.titleLabelX = this.imageWidth - (BACKGROUND_WIDTH / 2) - (this.font.width(this.title) / 2)
        this.titleLabelY = 6

        if (::cookButton.isInitialized) removeWidget(cookButton)
        val color = CampfirePotColor.entries[menu.containerData.get(COOKING_POT_COLOR_INDEX).coerceIn(0, CampfirePotColor.entries.lastIndex)]
        cookButton = CookButton(this.leftPos + 97, topPos + 56, menu.containerData.get(IS_LID_OPEN_INDEX) == 0, color) {
            val isLidClosed = menu.containerData.get(IS_LID_OPEN_INDEX) == 0
            sendToServer(ToggleCookingPotLidPacket(isLidClosed))
        }

        addRenderableWidget(cookButton)
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false)
    }

    override fun renderBg(
        context: GuiGraphics,
        partialTick: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        blitk(
            matrixStack = context.pose(),
            texture = BACKGROUND,
            x = leftPos, y = (height - BACKGROUND_HEIGHT) / 2,
            width = BACKGROUND_WIDTH, height = BACKGROUND_HEIGHT
        )

        val cookProgress = ceil(menu.getBurnProgress() * COOK_PROGRESS_WIDTH).toInt()
        blitk(
            matrixStack = context.pose(),
            texture = COOK_PROGRESS_SPRITE,
            x = leftPos + 96,
            y = topPos + 39,
            width = cookProgress,
            height = COOK_PROGRESS_HEIGHT,
            textureWidth = COOK_PROGRESS_WIDTH
        )
    }

    override fun render(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        cookButton.color = CampfirePotColor.entries[menu.containerData.get(COOKING_POT_COLOR_INDEX).coerceIn(0, CampfirePotColor.entries.lastIndex)]
        cookButton.selected = menu.containerData.get(IS_LID_OPEN_INDEX) == 0
        cookButton.setPosition(this.leftPos + 97, topPos + 56)

        if (this.recipeBookComponent.isVisible && this.widthTooNarrow) {
            this.renderBackground(context, mouseX, mouseY, partialTicks)
            this.recipeBookComponent.render(context, mouseX, mouseY, partialTicks)
        } else {
            super.render(context, mouseX, mouseY, partialTicks)
            this.recipeBookComponent.render(context, mouseX, mouseY, partialTicks)
            this.recipeBookComponent.renderGhostRecipe(context, this.leftPos, this.topPos, true, partialTicks)
        }

        val resultSlot = menu.slots[menu.resultSlotIndex]
        val optionalRecipe = menu.currentActiveRecipe
        if (optionalRecipe != null && !optionalRecipe.value.result.isEmpty && !resultSlot.hasItem()) {
            val resultItem = menu.previewItem

            RenderSystem.enableBlend()
            RenderSystem.setShaderColor(1F, 1F, 1F, 0.5F)

            context.renderFakeItem(resultItem, leftPos + resultSlot.x, topPos + resultSlot.y);
            context.renderItemDecorations(font, resultItem, leftPos + resultSlot.x, topPos + resultSlot.y)

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
            RenderSystem.disableBlend()

            if (isHovering(resultSlot.x, resultSlot.y, 16, 16, mouseX.toDouble(), mouseY.toDouble()) && menu.carried.isEmpty) {
                context.renderTooltip(font, getTooltipFromContainerItem(resultItem), resultItem.tooltipImage, mouseX, mouseY)
            }
        }

        this.renderTooltip(context, mouseX, mouseY)
        this.recipeBookComponent.renderTooltip(context, this.leftPos, this.topPos, mouseX, mouseY)
    }

    override fun renderSlot(guiGraphics: GuiGraphics, slot: Slot) {
        if (slot is SeasoningSlot && ((!menu.carried.isEmpty && !slot.mayPlace(menu.carried)) || (slot.hasItem() && !slot.mayPlace(slot.item))) ) {
            val x = slot.x
            val y = slot.y
            guiGraphics.fill(x,y,x+16,y+16,822018048)
        }
        super.renderSlot(guiGraphics, slot)
    }

    override fun recipesUpdated() {
        this.recipeBookComponent.recipesUpdated()
    }

    override fun getRecipeBookComponent(): RecipeBookComponent = this.recipeBookComponent

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val progressX = leftPos + 96
        val progressY = topPos + 39
        if (Cobblemon.implementation.isModInstalled("jei") &&
            mouseX >= progressX && mouseX < progressX + COOK_PROGRESS_WIDTH &&
            mouseY >= progressY && mouseY < progressY + COOK_PROGRESS_HEIGHT
        ) {
            CobblemonJeiPlugin.jeiRuntime?.recipesGui?.showTypes(
                listOf(CampfirePotRecipeCategory.RECIPE_TYPE)
            )
            return true
        }

        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
            this.focused = this.recipeBookComponent

            if (this.recipeBookComponent.isVisible) {
                val recipeBookFilterButton = (this.recipeBookComponent as RecipeBookComponentAccessor).filterButton
                if (recipeBookFilterButton.isMouseOver(mouseX, mouseY)) overrideRecipeBookFilterButton(
                    recipeBookFilterButton
                )
            }

            return true
        } else {
            return if (this.widthTooNarrow && this.recipeBookComponent.isVisible) true else super.mouseClicked(
                mouseX,
                mouseY,
                button
            )
        }
    }

    override fun renderTooltip(
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int
    ) {
        val progressX = leftPos + 96
        val progressY = topPos + 39
        if (Cobblemon.implementation.isModInstalled("jei") &&
            mouseX >= progressX && mouseX < progressX + COOK_PROGRESS_WIDTH &&
            mouseY >= progressY && mouseY < progressY + COOK_PROGRESS_HEIGHT
        ) {
            guiGraphics.renderTooltip(
                this.font,
                Component.translatable("jei.tooltip.show.recipes"),
                mouseX,
                mouseY
            )
            return
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY)
    }

    private fun overrideRecipeBookFilterButton(filterButton: StateSwitchingButton) {
        filterButton.initTextureValues(FILTER_BUTTON_SPRITES)
        filterButton.setTooltip(
            if (filterButton.isStateTriggered()) Tooltip.create(Component.translatable("cobblemon.container.campfire_pot.recipe_book.toggle_recipes"))
            else Tooltip.create(Component.translatable("gui.recipebook.toggleRecipes.all"))
        )
    }

    // Java nullability with kotlin is my greatest enemy, it'll complain but this works
    override fun slotClicked(slot: Slot?, slotId: Int, mouseButton: Int, type: ClickType) {
        super.slotClicked(slot, slotId, mouseButton, type)
        recipeBookComponent.slotClicked(slot)
    }
}
