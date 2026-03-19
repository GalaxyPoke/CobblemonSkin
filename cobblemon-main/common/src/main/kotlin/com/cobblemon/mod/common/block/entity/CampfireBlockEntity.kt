/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block.entity

import com.cobblemon.mod.common.CobblemonBlockEntities
import com.cobblemon.mod.common.CobblemonRecipeTypes
import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.cooking.Seasonings
import com.cobblemon.mod.common.api.cooking.getColourMixFromSeasonings
import com.cobblemon.mod.common.block.campfirepot.CampfireBlock
import com.cobblemon.mod.common.item.components.PotComponent
import com.cobblemon.mod.common.block.campfirepot.CookingPotMenu
import com.cobblemon.mod.common.client.particle.BedrockParticleOptionsRepository
import com.cobblemon.mod.common.client.particle.ParticleStorm
import com.cobblemon.mod.common.client.render.MatrixWrapper
import com.cobblemon.mod.common.client.sound.BlockEntitySoundTracker
import com.cobblemon.mod.common.client.sound.instances.CancellableSoundInstance
import com.cobblemon.mod.common.item.crafting.CookingPotRecipeBase
import com.cobblemon.mod.common.util.playSoundServer
import com.mojang.blaze3d.vertex.PoseStack
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FastColor
import net.minecraft.world.ContainerHelper
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.*
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity
import net.minecraft.world.level.block.entity.HopperBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3
import org.joml.Vector4f
import com.cobblemon.mod.common.item.CampfirePotItem
import java.util.*

class CampfireBlockEntity(pos: BlockPos, state: BlockState) : BaseContainerBlockEntity(
    CobblemonBlockEntities.CAMPFIRE,
    pos,
    state
), WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible, CraftingContainer {

    companion object {
        const val RESULT_SLOT = 0
        val CRAFTING_GRID_SLOTS = 1..9
        val SEASONING_SLOTS = 10..12
        const val ITEMS_SIZE = 13

        val PLAYER_INVENTORY_SLOTS = 13..39
        val PLAYER_HOTBAR_SLOTS = 40..48

        const val CRAFTING_GRID_WIDTH = 3
        const val PLAYER_INVENTORY_WIDTH = 9

        const val COOKING_TOTAL_TIME = 200
        const val COOKING_PROGRESS_PER_TICK = 2

        const val COOKING_PROGRESS_INDEX = 0
        const val COOKING_PROGRESS_TOTAL_TIME_INDEX = 1
        const val IS_LID_OPEN_INDEX = 2
        const val COOKING_POT_COLOR_INDEX = 3

        const val BASE_BROTH_COLOR = 0xFDFACF
        const val BASE_BROTH_BUBBLE_COLOR = 0xFFFEFDE4.toInt()

        fun clientTick(level: Level, pos: BlockPos, state: BlockState, campfireBlockEntity: CampfireBlockEntity) {
            if (!level.isClientSide) return

            val isCooking = state.getValue(CampfireBlock.COOKING)
            val isRunningSoundActive = BlockEntitySoundTracker.isActive(pos, campfireBlockEntity.runningSound.location)
            val isAmbientSoundActive = BlockEntitySoundTracker.isActive(pos, campfireBlockEntity.ambientSound.location)
            val containsItems = campfireBlockEntity.getSeasonings().isNotEmpty() || campfireBlockEntity.getIngredients().isNotEmpty()

            if (containsItems) {
                if (isCooking) {
                    BlockEntitySoundTracker.stop(pos, campfireBlockEntity.ambientSound.location)
                    if (!isRunningSoundActive) BlockEntitySoundTracker.play(pos, CancellableSoundInstance(campfireBlockEntity.runningSound, pos, true, 1.0f, 1.0f))
                } else {
                    BlockEntitySoundTracker.stop(pos, campfireBlockEntity.runningSound.location)
                    if (!isAmbientSoundActive) BlockEntitySoundTracker.play(pos, CancellableSoundInstance(campfireBlockEntity.ambientSound, pos, true, 1.0f, 1.0f))
                }
            } else {
                BlockEntitySoundTracker.stop(pos, campfireBlockEntity.runningSound.location)
                BlockEntitySoundTracker.stop(pos, campfireBlockEntity.ambientSound.location)
            }

            val seasonings = campfireBlockEntity.getSeasonings()
            if (seasonings.size != campfireBlockEntity.lastSeasoningStacks.size || seasonings.any { it !in campfireBlockEntity.lastSeasoningStacks }) {
                campfireBlockEntity.lastSeasoningStacks = seasonings

                campfireBlockEntity.brothColor =
                    getColourMixFromSeasonings(seasonings)
                        ?: BASE_BROTH_COLOR

                campfireBlockEntity.bubbleColor =
                    getColourMixFromSeasonings(seasonings, true)
                        ?: BASE_BROTH_BUBBLE_COLOR
            }

            if (campfireBlockEntity.particleCooldown > 0) {
                campfireBlockEntity.particleCooldown--
            } else {
                if (isCooking) {
                    val position = Vec3(pos.x + 0.5, pos.y + 0.5375, pos.z + 0.5)

                    campfireBlockEntity.particleEntityHandler(
                        position = position,
                        level = level,
                        particle = ResourceLocation("cobblemon", "broth_bubbles")
                    )
                }

                campfireBlockEntity.particleCooldown = 20
            }

            campfireBlockEntity.time++
        }

        fun serverTick(level: Level, pos: BlockPos, state: BlockState, campfireBlockEntity: CampfireBlockEntity) {
            if (level.isClientSide) return

            var hasChanged = false

            val craftingInput = CraftingInput.of(3, 3, campfireBlockEntity.items.subList(1, 10))

            fun <T : CookingPotRecipeBase> fetchRecipe(
                recipeType: RecipeType<T>
            ): Optional<RecipeHolder<CookingPotRecipeBase>> {
                val optional = level.recipeManager.getRecipeFor(recipeType, craftingInput, level)
                @Suppress("UNCHECKED_CAST")
                return optional.map { it as RecipeHolder<CookingPotRecipeBase> }
            }

            val isCookingBefore = campfireBlockEntity.cookingProgress > 0

            // Check for all Cooking Pot Recipe Types recipes
            val optionalRecipe = fetchRecipe(CobblemonRecipeTypes.COOKING_POT_COOKING)
                .orElseGet { fetchRecipe(CobblemonRecipeTypes.COOKING_POT_SHAPELESS).orElse(null) }

            if (optionalRecipe == null) {
                campfireBlockEntity.cookingProgress = 0
            }
            else {
                val cookingPotRecipe = optionalRecipe
                val recipe = cookingPotRecipe.value()
                val cookedItem = recipe.assemble(craftingInput, level.registryAccess())
                val resultSlotItem = campfireBlockEntity.getItem(0)

                recipe.applySeasoning(cookedItem, campfireBlockEntity.getSeasonings().filter { it.`is`(recipe.seasoningTag) })

                if (!campfireBlockEntity.blockState.getValue(CampfireBlock.LID)) {
                    campfireBlockEntity.cookingProgress = 0
                } else {
                    if (!resultSlotItem.isEmpty) {
                        if (!ItemStack.isSameItemSameComponents(
                                resultSlotItem,
                                cookedItem
                            ) || resultSlotItem.count + cookedItem.count > resultSlotItem.maxStackSize
                        ) {
                            campfireBlockEntity.cookingProgress = 0
                            return
                        }
                    }

                    campfireBlockEntity.cookingProgress += COOKING_PROGRESS_PER_TICK
                    if (campfireBlockEntity.cookingProgress == campfireBlockEntity.cookingTotalTime) {
                        campfireBlockEntity.cookingProgress = 0

                        if (!cookedItem.isEmpty) {
                            (campfireBlockEntity as RecipeCraftingHolder).recipeUsed = cookingPotRecipe

                            if (resultSlotItem.isEmpty) {
                                campfireBlockEntity.setItem(0, cookedItem)
                            } else {
                                resultSlotItem.grow(cookedItem.count)
                            }

                            campfireBlockEntity.consumeCraftingIngredients(
                                recipe,
                                level,
                                pos,
                                state,
                                campfireBlockEntity
                            )

                            level.playSoundServer(
                                position = pos.bottomCenter,
                                sound = CobblemonSounds.CAMPFIRE_POT_COOK,
                            )

                            hasChanged = true
                        }
                    }
                }
            }

            val isCookingNow = campfireBlockEntity.cookingProgress > 0

            if (isCookingBefore != isCookingNow) {
                hasChanged = true
                level.setBlock(pos, state.setValue(CampfireBlock.COOKING, isCookingNow), 3)
            }

            if (hasChanged) setChanged(level, pos, state)
        }
    }

    private val runningSound = CobblemonSounds.CAMPFIRE_POT_ACTIVE
    private val ambientSound = CobblemonSounds.CAMPFIRE_POT_AMBIENT
    private var cookingProgress: Int = 0
    private var cookingTotalTime: Int = COOKING_TOTAL_TIME
    private var items: NonNullList<ItemStack> = NonNullList.withSize(ITEMS_SIZE, ItemStack.EMPTY)
    private val recipesUsed: Object2IntOpenHashMap<ResourceLocation> = Object2IntOpenHashMap()
    private val quickCheck: RecipeManager.CachedCheck<CraftingInput, *> =
        RecipeManager.createCheck(CobblemonRecipeTypes.COOKING_POT_COOKING)
    private var potComponent: PotComponent? = null
    private var particleCooldown: Int = 0
    var brothColor: Int = BASE_BROTH_COLOR
    var bubbleColor: Int = BASE_BROTH_BUBBLE_COLOR
    var time: Int = 0

    /** Used to know if we should be changing the colour of the broth or if we can go with the previous colour. */
    var lastSeasoningStacks = listOf<ItemStack>()

    var dataAccess: ContainerData = object : ContainerData {
        override fun get(index: Int): Int {
            return when (index) {
                COOKING_PROGRESS_INDEX -> this@CampfireBlockEntity.cookingProgress
                COOKING_PROGRESS_TOTAL_TIME_INDEX -> this@CampfireBlockEntity.cookingTotalTime
                IS_LID_OPEN_INDEX -> if (this@CampfireBlockEntity.blockState.getValue(CampfireBlock.LID)) 0 else 1
                COOKING_POT_COLOR_INDEX -> (this@CampfireBlockEntity.getPotItem()?.item as? CampfirePotItem)?.color?.ordinal ?: 0
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                COOKING_PROGRESS_INDEX -> this@CampfireBlockEntity.cookingProgress = value
                COOKING_PROGRESS_TOTAL_TIME_INDEX -> this@CampfireBlockEntity.cookingTotalTime = value
                IS_LID_OPEN_INDEX -> this@CampfireBlockEntity.toggleLid(value == 1)
                COOKING_POT_COLOR_INDEX -> {}
            }
        }

        override fun getCount(): Int {
            return 4
        }
    }

    fun particleEntityHandler(position: Vec3, level: Level, particle: ResourceLocation): ParticleStorm {
        val wrapper = MatrixWrapper()
        val matrix = PoseStack()
        wrapper.updateMatrix(matrix.last().pose())
        wrapper.updatePosition(position)
        val effect = BedrockParticleOptionsRepository.getEffect(particle)
            ?: throw IllegalStateException("Particle with resource location $particle not found")

        val particleStorm = ParticleStorm(
            effect,
            wrapper,
            wrapper,
            level as ClientLevel,
            sourceAlive = { blockState.getValue(CampfireBlock.COOKING) },
            sourceVisible = { blockState.getValue(CampfireBlock.COOKING) },
            getParticleColor = {
                val red = FastColor.ARGB32.red(bubbleColor) / 255F
                val green = FastColor.ARGB32.green(bubbleColor) / 255F
                val blue = FastColor.ARGB32.blue(bubbleColor) / 255F
                val alpha = FastColor.ARGB32.alpha(bubbleColor) / 255F

                Vector4f(red, green, blue, alpha)
            }
        ).also {
            it.spawn()
        }

        return particleStorm
    }

    fun consumeCraftingIngredients(recipe: CookingPotRecipeBase, level: Level, pos: BlockPos, state: BlockState,  campfireBlockEntity: CampfireBlockEntity) {
        val remainderItems = mutableMapOf<Item, Int>() //This is so we don't spawn multiple entities for buckets

        fun consumeItem(slot: Int) {
            val itemInSlot = getItem(slot)
            if (!itemInSlot.isEmpty) {
                if (itemInSlot.item.hasCraftingRemainingItem()) {
                    remainderItems[itemInSlot.item.craftingRemainingItem!!] = (remainderItems[itemInSlot.item.craftingRemainingItem!!] ?: 0) + 1
                }
                itemInSlot.shrink(1)
                if (itemInSlot.count <= 0) setItem(slot, ItemStack.EMPTY)
            }
        }

        for (i in CRAFTING_GRID_SLOTS.first..CRAFTING_GRID_SLOTS.last) {
            consumeItem(i)
        }
        for (i in SEASONING_SLOTS.first..SEASONING_SLOTS.last) {
            if (getItem(i).`is`(recipe.seasoningTag) && recipe.seasoningProcessors.any { it.consumesItem(getItem(i)) }) consumeItem(i)
        }

        val direction = state.getValue(CampfireBlock.ITEM_DIRECTION)
        val container = HopperBlockEntity.getContainerAt(level, pos.relative(direction))

        for (remainder in remainderItems) {
            var remainderItem = ItemStack(remainder.key, remainder.value)

            if (container != null) {
                remainderItem = HopperBlockEntity.addItem(campfireBlockEntity, container, remainderItem, direction.opposite)
            }

            if (!remainderItem.isEmpty) {
                val spawnPos = Vec3.atCenterOf(pos).relative(direction, 0.7)
                val itemEntity = ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, remainderItem)
                itemEntity.setDeltaMovement(direction.stepX * 0.05, 0.0, direction.stepZ * 0.05)
                level.addFreshEntity(itemEntity) //Wanted to use default dispenser behavior but its too much speed
            }
        }
    }

    fun getSeasonings(): List<ItemStack> =
        items.subList(SEASONING_SLOTS.first, SEASONING_SLOTS.last + 1)
            .filterNotNull()
            .filter { !it.isEmpty }

    fun getIngredients(): List<ItemStack> =
        items.subList(CRAFTING_GRID_SLOTS.first, CRAFTING_GRID_SLOTS.last + 1)
            .filterNotNull()
            .filter { !it.isEmpty }

    override fun getDefaultName(): Component {
        return Component.translatable("cobblemon.container.campfire_pot")
    }

    override fun getWidth() = CRAFTING_GRID_WIDTH
    override fun getHeight() = CRAFTING_GRID_WIDTH

    override fun getItems(): NonNullList<ItemStack> {
        level?.let { onItemUpdate(it) }
        return this.items
    }

    override fun setItems(items: NonNullList<ItemStack>) {
        this.items.clear()
        this.items.addAll(items)
        level?.let { onItemUpdate(it) }
    }

    override fun createMenu(
        containerId: Int,
        inventory: Inventory
    ): AbstractContainerMenu {
        return CookingPotMenu(containerId, inventory, this, this.dataAccess)
    }

    override fun getContainerSize() = this.items.size
    override fun getSlotsForFace(side: Direction) = (0..12).toList().toIntArray()

    override fun setRecipeUsed(recipe: RecipeHolder<*>?) {
        if (recipe != null) {
            val resourceLocation = recipe.id()
            this.recipesUsed.addTo(resourceLocation, 1)
        }
    }

    override fun getRecipeUsed() = null
    override fun fillStackedContents(contents: StackedContents) {
        for (itemStack in this.items) {
            contents.accountSimpleStack(itemStack);
        }
    }

    override fun getItem(slot: Int): ItemStack {
        return if (slot in 0 until items.size) items[slot] else ItemStack.EMPTY
    }

    fun getPotItem(): ItemStack? {
        return potComponent?.potItem
    }

    fun setPotItem(stack: ItemStack?) {
        this.potComponent = PotComponent(stack ?: ItemStack.EMPTY) // Ensure a non-null value is passed
        setChanged()
        level?.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)

        tag.putInt("CookingProgress", this.cookingProgress)

        ContainerHelper.saveAllItems(tag, this.items, registries)
        potComponent?.let { component ->
            PotComponent.CODEC.encodeStart(NbtOps.INSTANCE, component)
                .result()
                ?.ifPresent { encoded -> tag.put("PotComponent", encoded) }
        }
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)

        this.cookingProgress = tag.getInt("CookingProgress")

        // Minecraft doesn't save empty item stacks to the items tag, so we have to manually clear them
        // otherwise they would never clear and seasonings would always render wrongly

        clearContent()
        ContainerHelper.loadAllItems(tag, this.items, registries)

        if (tag.contains("PotComponent")) {
            val component = PotComponent.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("PotComponent"))
                .result()
                ?.orElse(null)
            potComponent = component
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(registryLookup: HolderLookup.Provider): CompoundTag {
        return saveWithoutMetadata(registryLookup)
    }

    private fun onItemUpdate(level: Level) {
        val oldState = level.getBlockState(blockPos)
        level.sendBlockUpdated(blockPos, oldState, level.getBlockState(blockPos), Block.UPDATE_ALL)
        level.updateNeighbourForOutputSignal(blockPos, level.getBlockState(blockPos).block)
        setChanged()
        level.sendBlockUpdated(blockPos, oldState, level.getBlockState(blockPos), Block.UPDATE_ALL)
    }

    override fun setRemoved() {
        cookingProgress = 0
        super.setRemoved()

        if (level?.isClientSide == true) {
            BlockEntitySoundTracker.stop(blockPos, runningSound.location)
            BlockEntitySoundTracker.stop(blockPos, ambientSound.location)
        }
    }

    fun toggleLid(isOpen: Boolean) {
        level?.let { lvl ->
            lvl.playSoundServer(
                position = blockPos.center,
                sound = if (isOpen) CobblemonSounds.CAMPFIRE_POT_OPEN else CobblemonSounds.CAMPFIRE_POT_CLOSE
            )
            lvl.gameEvent(
                null,
                if (isOpen) GameEvent.BLOCK_OPEN else GameEvent.BLOCK_CLOSE,
                blockPos
            )
            lvl.setBlock(this.blockPos, this.blockState.setValue(CampfireBlock.LID, !isOpen), 3)
        }
        setChanged()
    }

    override fun canTakeItemThroughFace(index: Int, stack: ItemStack, direction: Direction): Boolean {
        return index == RESULT_SLOT
    }

    override fun canPlaceItemThroughFace(index: Int, itemStack: ItemStack, direction: Direction?): Boolean {
        return if(direction == Direction.UP && Seasonings.isSeasoning(itemStack)){
            SEASONING_SLOTS.contains(index)
        } else {
            CRAFTING_GRID_SLOTS.contains(index)
        }
    }


}
