/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.campfirepot

import com.cobblemon.mod.common.CobblemonMenuType
import com.cobblemon.mod.common.CobblemonRecipeBookTypes.COOKING_POT
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.block.entity.CampfireBlockEntity
import com.cobblemon.mod.common.item.crafting.CookingPotRecipe
import com.cobblemon.mod.common.item.crafting.CookingPotRecipeBase
import net.minecraft.recipebook.ServerPlaceRecipe
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerListener
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.inventory.RecipeBookMenu
import net.minecraft.world.inventory.RecipeBookType
import net.minecraft.world.inventory.ResultContainer
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import java.util.*

class CookingPotMenu : RecipeBookMenu<CraftingInput, CookingPotRecipeBase>, ContainerListener {
    private val player: Player
    private val level: Level
    private val playerInventory: Inventory
    val container: CraftingContainer
    private val resultContainer: ResultContainer
    val containerData: ContainerData
    private val recipeType: RecipeType<CookingPotRecipe> = CobblemonRecipeTypes.COOKING_POT_COOKING
    private val quickCheck = RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING)
    var currentActiveRecipe: RecipeHolder<CookingPotRecipeBase>? = null
    var previewItem = ItemStack.EMPTY

    constructor(containerId: Int, playerInventory: Inventory) : super(CobblemonMenuType.COOKING_POT, containerId) {
        this.playerInventory = playerInventory
        this.container = CookingPotContainer(this,
            CampfireBlockEntity.CRAFTING_GRID_WIDTH,
            CampfireBlockEntity.CRAFTING_GRID_WIDTH
        )
        this.resultContainer = ResultContainer()
        this.containerData = SimpleContainerData(4)
        this.addDataSlots(containerData)
        this.player = playerInventory.player
        this.level = playerInventory.player.level()
        initializeSlots(playerInventory)
        this.addSlotListener(this)
    }

    constructor(containerId: Int, playerInventory: Inventory, container: CraftingContainer, containerData: ContainerData) : super(
        CobblemonMenuType.COOKING_POT, containerId) {
        this.playerInventory = playerInventory
        this.container = container
        this.containerData = containerData
        this.addDataSlots(containerData)
        this.resultContainer = ResultContainer()
        this.player = playerInventory.player
        this.level = playerInventory.player.level()
        container.startOpen(playerInventory.player)
        initializeSlots(playerInventory)
        this.addSlotListener(this)
    }

    private fun initializeSlots(playerInventory: Inventory) {
        val resultSlotX = 128
        val resultSlotY = 55

        addSlot(CookingPotResultSlot(this.container, CampfireBlockEntity.RESULT_SLOT, resultSlotX, resultSlotY))

        for ((index, slotIndex) in CampfireBlockEntity.CRAFTING_GRID_SLOTS.withIndex()) {
            val i = index / CampfireBlockEntity.CRAFTING_GRID_WIDTH
            val j = index % CampfireBlockEntity.CRAFTING_GRID_WIDTH
            addSlot(Slot(this.container, slotIndex, 33 + j * 18, 18 + i * 18))
        }

        for ((index, slotIndex) in CampfireBlockEntity.SEASONING_SLOTS.withIndex()) {
            addSlot(SeasoningSlot(this, this.container, slotIndex, 110 + index * 18, 18))
        }

        for ((index, _) in CampfireBlockEntity.PLAYER_INVENTORY_SLOTS.withIndex()) {
            val i = index / CampfireBlockEntity.PLAYER_INVENTORY_WIDTH
            val j = index % CampfireBlockEntity.PLAYER_INVENTORY_WIDTH
            addSlot(Slot(playerInventory, index + 9, 8 + j * 18, 84 + i * 18))
        }

        for ((index, _) in CampfireBlockEntity.PLAYER_HOTBAR_SLOTS.withIndex()) {
            addSlot(Slot(playerInventory, index, 8 + index * 18, 142))
        }
    }

    override fun broadcastChanges() {
        super.broadcastChanges()
    }

    override fun handlePlacement(placeAll: Boolean, recipe: RecipeHolder<*>, player: ServerPlayer) {
        val recipeValue = recipe.value()
        if (recipeValue is CookingPotRecipeBase) {
            @Suppress("UNCHECKED_CAST")
            val castedRecipe = recipe as RecipeHolder<CookingPotRecipeBase>

            // Save seasoning contents
            val seasoningSlots = CampfireBlockEntity.SEASONING_SLOTS
            val preservedSeasonings = seasoningSlots.map { container.getItem(it).copy() }

            this.beginPlacingRecipe()
            try {
                val serverPlaceRecipe = ServerPlaceRecipe(this)
                serverPlaceRecipe.recipeClicked(player, castedRecipe, placeAll)
            } finally {
                this.finishPlacingRecipe(castedRecipe)
            }

            seasoningSlots.forEachIndexed { index, slot ->
                container.setItem(slot, preservedSeasonings[index])
            }
        } else {
            throw IllegalArgumentException("Unsupported recipe type: ${recipeValue::class.java.name}")
        }
    }

    override fun removed(player: Player) {
        super.removed(player)
    }

    override fun fillCraftSlotsStackedContents(itemHelper: StackedContents) {
        this.container.fillStackedContents(itemHelper)
    }

    override fun clearCraftingContent() {
        container.clearContent()
    }

    override fun recipeMatches(recipe: RecipeHolder<CookingPotRecipeBase>): Boolean {
        val craftInput = CraftingInput.of(3,3, container.items.subList(1,10))
        val recipeValue = recipe.value()
        return if (recipeValue is CookingPotRecipeBase) {
            recipeValue.matches(craftInput, level)
        } else {
            false
        }
    }

    private fun recalculateRecipe() {
        val craftInput = CraftingInput.of(3,3, container.items.subList(1,10))
        fun <T : CookingPotRecipeBase> fetchRecipe(
            recipeType: RecipeType<T>
        ): Optional<RecipeHolder<CookingPotRecipeBase>> {
            val optional = level.recipeManager.getRecipeFor(recipeType, craftInput, level)
            @Suppress("UNCHECKED_CAST")
            return optional.map { it as RecipeHolder<CookingPotRecipeBase> }
        }

        val recipe = fetchRecipe(CobblemonRecipeTypes.COOKING_POT_COOKING)
            .orElseGet { fetchRecipe(CobblemonRecipeTypes.COOKING_POT_SHAPELESS).orElse(null) }

        currentActiveRecipe = recipe
        if (recipe != null) {
            previewItem = recipe.value.assemble(craftInput, level.registryAccess())
            // Apply seasoning to the preview item
            recipe.value.applySeasoning(
                previewItem,
                container.items.subList(CampfireBlockEntity.SEASONING_SLOTS.first,
                    CampfireBlockEntity.SEASONING_SLOTS.last + 1)
                    .filterNotNull()
                    .filter { !it.isEmpty && it.`is`(recipe.value.seasoningTag) })

        } else previewItem = ItemStack.EMPTY
    }

    override fun getResultSlotIndex(): Int {
        return CampfireBlockEntity.RESULT_SLOT
    }

    override fun getGridWidth(): Int {
        return CampfireBlockEntity.CRAFTING_GRID_WIDTH
    }

    override fun getGridHeight(): Int {
        return CampfireBlockEntity.CRAFTING_GRID_WIDTH
    }

    override fun getSize(): Int {
        return CampfireBlockEntity.ITEMS_SIZE
    }

    fun getBurnProgress(): Float {
        val i = this.containerData.get(0)
        val j = this.containerData.get(1)
        return if (j != 0 && i != 0) {
            Mth.clamp((i.toFloat() / j.toFloat()), 0.0F, 1.0F)
        } else {
            0.0F;
        }
    }

    override fun getRecipeBookType(): RecipeBookType {
        return COOKING_POT
    }

    override fun shouldMoveToInventory(slotIndex: Int): Boolean {
        return !CampfireBlockEntity.SEASONING_SLOTS.contains(slotIndex)
    }

    override fun quickMoveStack(
        player: Player,
        index: Int
    ): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]

        if (slot.hasItem()) {
            val slotItemStack = slot.item
            itemStack = slotItemStack.copy()

            if (index == CampfireBlockEntity.RESULT_SLOT) {
                if (!this.moveItemStackTo(slotItemStack, CampfireBlockEntity.PLAYER_INVENTORY_SLOTS.first, CampfireBlockEntity.PLAYER_HOTBAR_SLOTS.last + 1, false)) {
                    return ItemStack.EMPTY
                }

                slot.onQuickCraft(slotItemStack, itemStack);
            } else if (index in CampfireBlockEntity.PLAYER_INVENTORY_SLOTS || index in CampfireBlockEntity.PLAYER_HOTBAR_SLOTS) {
                if (Seasonings.isSeasoning(slotItemStack)) {
                    if (!this.moveItemStackTo(slotItemStack, CampfireBlockEntity.CRAFTING_GRID_SLOTS.first, CampfireBlockEntity.CRAFTING_GRID_SLOTS.last + 1, false) &&
                        !this.moveItemStackTo(slotItemStack, CampfireBlockEntity.SEASONING_SLOTS.first, CampfireBlockEntity.SEASONING_SLOTS.last + 1, false)
                    ) {
                        return ItemStack.EMPTY
                    }
                } else if (!this.moveItemStackTo(slotItemStack, CampfireBlockEntity.CRAFTING_GRID_SLOTS.first, CampfireBlockEntity.CRAFTING_GRID_SLOTS.last + 1, false)) {
                    return ItemStack.EMPTY
                }
            } else if (index in CampfireBlockEntity.CRAFTING_GRID_SLOTS || index in CampfireBlockEntity.SEASONING_SLOTS) {
                if (!this.moveItemStackTo(slotItemStack, CampfireBlockEntity.PLAYER_INVENTORY_SLOTS.first, CampfireBlockEntity.PLAYER_HOTBAR_SLOTS.last + 1, false)) {
                    return ItemStack.EMPTY
                }
            }

            if (slotItemStack.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (slotItemStack.count == itemStack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(player, slotItemStack)
        }

        return itemStack
    }

    override fun stillValid(player: Player): Boolean {
        return this.container.stillValid(player)
    }

    override fun slotChanged(containerToSend: AbstractContainerMenu, dataSlotIndex: Int, stack: ItemStack) {
        if (CampfireBlockEntity.CRAFTING_GRID_SLOTS.contains(dataSlotIndex) || CampfireBlockEntity.SEASONING_SLOTS.contains(dataSlotIndex)) recalculateRecipe()
        broadcastChanges()
    }

    override fun dataChanged(
        containerMenu: AbstractContainerMenu,
        dataSlotIndex: Int,
        value: Int
    ) {
    }
}
