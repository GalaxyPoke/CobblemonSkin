/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.entity.boat

import com.cobblemon.mod.common.CobblemonEntities
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.HasCustomInventoryScreen
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.monster.piglin.PiglinAi
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.vehicle.ContainerEntity
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.storage.loot.LootTable

@Suppress("unused")
class CobblemonChestBoatEntity(entityType: EntityType<CobblemonChestBoatEntity>, world: Level) : CobblemonBoatEntity(entityType, world), HasCustomInventoryScreen, ContainerEntity {

    constructor(world: Level) : this(CobblemonEntities.CHEST_BOAT, world)

    // This exists cause super passes in vanilla boat entity type
    constructor(world: Level, x: Double, y: Double, z: Double) : this(CobblemonEntities.CHEST_BOAT, world) {
        this.setPos(x, y, z)
        this.xo = x
        this.yo = y
        this.zo = z
    }

    private var inventory = this.emptyInventory()
    private var lootTableId: ResourceKey<LootTable>? = null
    private var lootTableSeed = 0L

    override fun openCustomInventoryScreen(player: Player) {
        player.openMenu(this)
        if (!player.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player)
            PiglinAi.angerNearbyPiglins(player, true)
        }
    }

    override fun getMaxPassengers() = 1

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        this.addChestVehicleSaveData(compound, this.registryAccess())
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        this.readChestVehicleSaveData(compound, this.registryAccess())
    }

    public override fun destroy(source: DamageSource) {
        this.destroy(this.getDropItem())
        this.chestVehicleDestroyed(source, this.level(), this)
    }

    override fun remove(reason: RemovalReason) {
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this)
        }

        super.remove(reason)
    }

    override fun interact(player: Player, hand: InteractionHand): InteractionResult {
        if (!player.isSecondaryUseActive) {
            val interactionResult = super.interact(player, hand)
            if (interactionResult != InteractionResult.PASS) {
                return interactionResult
            }
        }

        if (this.canAddPassenger(player) && !player.isSecondaryUseActive) {
            return InteractionResult.PASS
        } else {
            val interactionResult: InteractionResult = this.interactWithContainerVehicle(player)
            if (interactionResult.consumesAction()) {
                this.gameEvent(GameEvent.CONTAINER_OPEN, player)
                PiglinAi.angerNearbyPiglins(player, true)
            }

            return interactionResult
        }
    }


    override fun clearContent() = this.clearItemStacks()

    override fun getContainerSize(): Int = INVENTORY_SLOTS

    override fun getItem(slot: Int): ItemStack = this.getChestVehicleItem(slot)

    override fun removeItem(slot: Int, amount: Int): ItemStack = this.removeChestVehicleItem(slot, amount)

    override fun removeItemNoUpdate(slot: Int): ItemStack = this.removeChestVehicleItemNoUpdate(slot)

    override fun setItem(slot: Int, stack: ItemStack) = this.setChestVehicleItem(slot, stack)

    override fun getSlot(slot: Int): SlotAccess? = this.getChestVehicleSlot(slot)

    override fun setChanged() {}

    override fun stillValid(player: Player): Boolean = this.isChestVehicleStillValid(player)

    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu? {
        if (this.lootTableId != null && player.isSpectator) {
            return null
        }
        this.unpackChestVehicleLootTable(playerInventory.player)
        return ChestMenu.threeRows(syncId, playerInventory, this)
    }

    override fun getLootTable() = lootTableId

    override fun setLootTable(lootTable: ResourceKey<LootTable>?) {
        this.lootTableId = lootTable
    }

    override fun getLootTableSeed(): Long = this.lootTableSeed

    override fun setLootTableSeed(lootTableSeed: Long) {
        this.lootTableSeed = lootTableSeed
    }

    override fun getItemStacks(): NonNullList<ItemStack> = this.inventory

    override fun clearItemStacks() {
        this.inventory = this.emptyInventory()
    }

    override fun getDropItem(): Item = this.boatType.chestBoatItem

    private fun emptyInventory(): NonNullList<ItemStack> = NonNullList.withSize(INVENTORY_SLOTS, ItemStack.EMPTY)

    override fun stopOpen(player: Player) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player))
    }

    companion object {

        private const val INVENTORY_SLOTS = 27

    }

}