/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.item

import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.cooking.Flavour
import com.cobblemon.mod.common.api.cooking.PokePuffUtils
import com.cobblemon.mod.common.api.item.PokemonSelectingItem
import com.cobblemon.mod.common.pokemon.Nature
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim
import net.minecraft.world.level.Level

class PokePuffItem : Item(Properties().stacksTo(64)), PokemonSelectingItem {
    override val bagItem = null

    companion object {
        const val LIKED_FLAVOR_MULTIPLIER = 10
        const val DISLIKED_FLAVOR_MULTIPLIER = 7 // if they don't like it maybe have a bigger negative effect on the friendship? might make sense
    }

    override fun getName(stack: ItemStack): Component {
        val dominantFlavours = stack.get(CobblemonItemComponents.FLAVOUR)?.getDominantFlavours()
        val flavour = when {
            dominantFlavours == null || dominantFlavours.isEmpty() -> "plain"
            dominantFlavours.size > 1 -> "mild"
            else -> dominantFlavours.first().name.lowercase()
        }

        val ingredients = stack.get(CobblemonItemComponents.INGREDIENT)
            ?.ingredientIds?.map { it.toString() } ?: emptyList()

        val hasSugar = "minecraft:sugar" in ingredients
        val hasSweet = ingredients.any {
            it.startsWith("cobblemon:") && it.endsWith("_sweet")
        }

        val prefix = when {
            hasSugar && hasSweet -> "deluxe"
            hasSweet -> "fancy"
            hasSugar -> "frosted"
            else -> null
        }

        val isCreamPuff = (flavour == "plain")
        val baseKey = if (isCreamPuff) "item.cobblemon.cream_puff" else "item.cobblemon.poke_puff"
        val finalKey = if (prefix != null) "$baseKey.$prefix" else baseKey

        return if (isCreamPuff) {
            // No flavor inserted if it's Cream Puff
            Component.translatable(finalKey)
        } else {
            // Insert flavor name for normal Poke Puffs
            val flavourTranslationKey = "flavour.cobblemon.poke_puff.$flavour"
            Component.translatable(finalKey, Component.translatable(flavourTranslationKey))
        }
    }

    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        val stack = user.getItemInHand(hand)
        val isPlain = getFlavorType(stack) == "plain"

        return if (isPlain) {
            if (!user.foodData.needsFood()) {
                return InteractionResultHolder.fail(stack)
            }
            user.startUsingItem(hand)
            InteractionResultHolder.consume(stack)
        } else {
            if (user is ServerPlayer) {
                super<PokemonSelectingItem>.use(user, stack)
            } else {
                InteractionResultHolder.pass(stack)
            }
        }
    }

    override fun applyToPokemon(player: ServerPlayer, stack: ItemStack, pokemon: Pokemon): InteractionResultHolder<ItemStack> {
        if (!canUseOnPokemon(stack, pokemon)) {
            return InteractionResultHolder.fail(stack)
        }

        // Feed the Pokémon 4 fullness points
        pokemon.feedPokemon(4)

        val friendshipChange = PokePuffUtils.calculateFriendshipChange(stack, pokemon.nature)

        if (friendshipChange != 0) {
            val current = pokemon.friendship
            val newValue = (current + friendshipChange).coerceIn(0, 255)

            if (newValue != current) {
                pokemon.setFriendship(newValue)
                pokemon.entity?.playSound(SoundEvents.PLAYER_BURP, 1F, 1F)
                stack.consume(1, player)
                return InteractionResultHolder.success(stack)
            }
        }
        return InteractionResultHolder.pass(stack)
    }

    private fun isPlainPuff(stack: ItemStack): Boolean {
        val flavours = stack.get(CobblemonItemComponents.FLAVOUR)?.flavours ?: return true
        return flavours.values.all { it == 0 }
    }

    private fun Nature.isNeutral(): Boolean {
        return this.favouriteFlavour == this.dislikedFlavour
    }

    override fun getUseAnimation(stack: ItemStack): UseAnim {
        val isPlain = isPlainPuff(stack)
        return if (isPlain) UseAnim.EAT else UseAnim.NONE
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity): Int {
        return if (getFlavorType(stack) == "plain") 32 else 0
    }

    override fun getEatingSound(): SoundEvent = SoundEvents.GENERIC_EAT
    override fun getDrinkingSound(): SoundEvent = SoundEvents.GENERIC_EAT

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        if (!world.isClientSide && user is Player && getFlavorType(stack) == "plain") {
            val ingredients = stack.get(CobblemonItemComponents.INGREDIENT)?.ingredientIds?.map { it.toString() } ?: emptyList()
            val hasSugar = "minecraft:sugar" in ingredients
            val hasSweet = ingredients.any { it.startsWith("cobblemon:") && it.endsWith("_sweet") }

            val (nutrition, saturation) = when {
                hasSugar && hasSweet -> 6 to 4f    // deluxe
                hasSweet -> 4 to 2.8f              // fancy
                hasSugar -> 3 to 2.4f              // frosted
                else -> 2 to 2f                    // plain
            }

            if (user.foodData.needsFood()) {
                user.foodData.eat(nutrition, saturation)
                stack.consume(1, user)
            }
        }

        return super.finishUsingItem(stack, world, user)
    }


    override fun canUseOnPokemon(stack: ItemStack, pokemon: Pokemon): Boolean {
        return getFriendshipDelta(stack, pokemon) != 0 && super.canUseOnPokemon(stack, pokemon)
    }

    private fun getFriendshipDelta(stack: ItemStack, pokemon: Pokemon): Int {
        val flavour = getDominantFlavour(stack) ?: return 0
        val nature = pokemon.nature

        return when {
            flavour == Flavour.MILD && nature.isNeutral() -> 5
            flavour == nature.favouriteFlavour -> 10
            flavour == nature.dislikedFlavour -> -10
            else -> -10
        }
    }

    private fun getDominantFlavour(stack: ItemStack): Flavour? {
        val flavours = stack.get(CobblemonItemComponents.FLAVOUR)?.flavours ?: return null
        val max = flavours.values.maxOrNull() ?: return null
        val dominant = flavours.filterValues { it == max }.keys

        return when {
            dominant.isEmpty() -> null
            dominant.size > 1 -> Flavour.MILD
            else -> dominant.first()
        }
    }

    private fun getFlavorType(stack: ItemStack): String {
        val flavour = getDominantFlavour(stack)
        return flavour?.name?.lowercase() ?: "plain"
    }
}