/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.layer

import com.bedrockk.molang.runtime.value.DoubleValue
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.tags.CobblemonItemTags
import com.cobblemon.mod.common.client.render.item.HeldItemRenderer
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.entity.PoseType
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.util.DataKeys
import com.cobblemon.mod.common.util.isPokemonEntity
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import java.util.UUID
import net.minecraft.client.Minecraft
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class PokemonOnShoulderRenderer<T : Player>(renderLayerParent: RenderLayerParent<T, PlayerModel<T>>) : RenderLayer<T, PlayerModel<T>>(renderLayerParent) {

    val context = RenderContext().also {
        it.put(RenderContext.RENDER_STATE, RenderContext.RenderState.WORLD)
    }

    private val heldItemRenderer = HeldItemRenderer()

    var leftState = FloatingState()
    var lastRenderedLeft: ShoulderData? = null
    var rightState = FloatingState()
    var lastRenderedRight: ShoulderData? = null

    override fun render(
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        livingEntity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float
    ) {
        // It's unclear why Minecraft's providing a partial ticks based on 60FPS regardless of the real FPS.
        // Whatever - the delta manager works correctly.
        val realPartialTicks = Minecraft.getInstance().timer.realtimeDeltaTicks
        this.render(matrixStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, realPartialTicks, ageInTicks, netHeadYaw, headPitch, true)
        this.render(matrixStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, realPartialTicks, ageInTicks, netHeadYaw, headPitch, false)
    }

    fun configureState(state: FloatingState, model: PosableModel, leftShoulder: Boolean, shoulderData: ShoulderData): FloatingState {
        state.currentModel = model
        state.setPoseToFirstSuitable(if (leftShoulder) PoseType.SHOULDER_LEFT else PoseType.SHOULDER_RIGHT)

        state.runtime.environment.query.addFunction("is_holding_item") { return@addFunction DoubleValue(shoulderData.shownItem.let {
            !it.isEmpty && !it.`is`(CobblemonItemTags.WEARABLE_HAT_ITEMS) && !it.`is`(CobblemonItemTags.WEARABLE_FACE_ITEMS)
        }) }
        state.runtime.environment.query.addFunction("is_wearing_hat") { return@addFunction DoubleValue(shoulderData.shownItem.`is`(CobblemonItemTags.WEARABLE_HAT_ITEMS)) }
        state.runtime.environment.query.addFunction("is_wearing_face") { return@addFunction DoubleValue(shoulderData.shownItem.`is`(CobblemonItemTags.WEARABLE_FACE_ITEMS)) }

        return state
    }

    private fun render(
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        livingEntity: T,
        limbSwing: Float,
        limbSwingAmount: Float,
        partialTicks: Float,
        ageInTicks: Float,
        netHeadYaw: Float,
        headPitch: Float,
        pLeftShoulder: Boolean
    ) {
        val compoundTag = if (pLeftShoulder) livingEntity.shoulderEntityLeft else livingEntity.shoulderEntityRight
        if (compoundTag.isPokemonEntity()) {
            livingEntity.level().profiler.push("shoulder_render_".plus(if(pLeftShoulder) "left" else "right"))
            val cache = playerCache.getOrPut(livingEntity.uuid) { ShoulderCache() }
            val shoulderData = shoulderDataFrom(compoundTag)
            if (pLeftShoulder) cache.lastKnownLeft = shoulderData else cache.lastKnownRight = shoulderData

            if (shoulderData == null){
                // Nothing to do
                return
            }

            matrixStack.pushPose()
            var state = FloatingState()
            state.currentAspects = shoulderData.aspects
            val model = VaryingModelRepository.getPoser(shoulderData.species.resourceIdentifier, state)
            model.context = context
            context.put(RenderContext.SPECIES, shoulderData.species.resourceIdentifier)
            context.put(RenderContext.ASPECTS, shoulderData.aspects)
            val scale = shoulderData.form.baseScale * shoulderData.scaleModifier
            val width = shoulderData.form.hitbox.width
            val heightOffset = -1.5 * scale
            val widthOffset = width / 2 - 0.7
            // If they're sneaking, the pokemon needs to rotate a little bit and push forward
            // Shoulders move a bit when sneaking which is why the translation is necessary.
            // Shoulder exact rotation according to testing (miasmus) is 0.4 radians, the -0.15 is eyeballed though.
            if (livingEntity.isCrouching) {
                matrixStack.mulPose(Axis.XP.rotation(0.4F))
                matrixStack.translate(0F, 0F, -0.15F)
            }
            matrixStack.translate(
                if (pLeftShoulder) -widthOffset else widthOffset,
                (if (livingEntity.isCrouching) heightOffset + 0.2 else heightOffset),
                0.0
            )

            matrixStack.scale(scale, scale, scale)
            matrixStack.translate(0f, 1.5f, 0f)

            state = if (pLeftShoulder && shoulderData != lastRenderedLeft) {
                leftState = configureState(state, model, true, shoulderData)
                lastRenderedLeft = shoulderData
                leftState
            } else if (!pLeftShoulder && shoulderData != lastRenderedRight) {
                rightState = configureState(state, model, false, shoulderData)
                lastRenderedRight = shoulderData
                rightState
            } else {
                if (pLeftShoulder) leftState else rightState
            }
            state.updatePartialTicks(partialTicks)
            context.put(RenderContext.POSABLE_STATE, state)
            state.currentModel = model
            val vertexConsumer = buffer.getBuffer(RenderType.entityCutout(VaryingModelRepository.getTexture(shoulderData.species.resourceIdentifier, state)))
            val i = LivingEntityRenderer.getOverlayCoords(livingEntity, 0.0f)

            model.applyAnimations(
                entity = null,
                state = state,
                headYaw = netHeadYaw,
                headPitch = headPitch,
                limbSwing = limbSwing,
                limbSwingAmount = limbSwingAmount,
                ageInTicks = livingEntity.tickCount.toFloat()
            )
            model.render(context, matrixStack, vertexConsumer, packedLight, i, -0x1)
            model.withLayerContext(buffer, state, VaryingModelRepository.getLayers(shoulderData.species.resourceIdentifier, state)) {
                model.render(context, matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1)
            }

            heldItemRenderer.renderOnModel(
                shoulderData.shownItem,
                state,
                matrixStack,
                buffer,
                packedLight
            )

            model.setDefault()
            matrixStack.popPose()
            livingEntity.level().profiler.pop()
        }
        else {
            val entry = playerCache.getOrPut(livingEntity.uuid) { ShoulderCache() }
            if (pLeftShoulder) entry.lastKnownLeft = null else entry.lastKnownRight = null
        }
    }

    private data class ShoulderCache(
        var lastKnownLeft: ShoulderData? = null,
        var lastKnownRight: ShoulderData? = null
    )

    data class ShoulderData(
        val uuid: UUID,
        val species: Species,
        val form: FormData,
        val aspects: Set<String>,
        val scaleModifier: Float,
        val shownItem: ItemStack
    )

    companion object {

        private val playerCache = hashMapOf<UUID, ShoulderCache>()

        private fun extractUuid(shoulderNbt: CompoundTag): UUID {
            if (!shoulderNbt.contains(DataKeys.SHOULDER_UUID)) {
                return shoulderNbt.getCompound(DataKeys.POKEMON).getUUID(DataKeys.POKEMON_UUID)
            }
            return shoulderNbt.getUUID(DataKeys.SHOULDER_UUID)
        }

        private fun extractData(shoulderNbt: CompoundTag, pokemonUUID: UUID): ShoulderData? {
            // To not crash with existing ones, this will still have the aspect issue
            if (!shoulderNbt.contains(DataKeys.SHOULDER_SPECIES)) {
                return Pokemon.CLIENT_CODEC.decode(NbtOps.INSTANCE, shoulderNbt.getCompound(DataKeys.POKEMON))
                    .map { it.first }
                    .mapOrElse({
                        ShoulderData(
                            pokemonUUID,
                            it.species,
                            it.form,
                            it.aspects,
                            it.scaleModifier,
                            it.heldItem
                        )
                    }, { null })
            }
            val species =
                PokemonSpecies.getByIdentifier(ResourceLocation.parse(shoulderNbt.getString(DataKeys.SHOULDER_SPECIES)))
                    ?: return null
            val formName = shoulderNbt.getString(DataKeys.SHOULDER_FORM)
            val form = species.forms.firstOrNull { it.name == formName } ?: species.standardForm
            val aspects =
                shoulderNbt.getList(DataKeys.SHOULDER_ASPECTS, Tag.TAG_STRING.toInt()).map { it.asString }.toSet()
            val scaleModifier = shoulderNbt.getFloat(DataKeys.SHOULDER_SCALE_MODIFIER)
            val shownItem = Minecraft.getInstance().level?.registryAccess()
                ?.let { ItemStack.parseOptional(it, shoulderNbt.getCompound(DataKeys.SHOULDER_ITEM)) }
                ?: ItemStack.EMPTY
            return ShoulderData(pokemonUUID, species, form, aspects, scaleModifier, shownItem)
        }

        /**
         * Checks if a player has shoulder data cached.
         *
         * @param player The player being checked.
         * @return A [Pair] with [Pair.first] and [Pair.second] being the respective shoulder.
         */
        @JvmStatic
        fun shoulderDataOf(player: Player): Pair<ShoulderData?, ShoulderData?> {
            val cache = playerCache[player.uuid] ?: return Pair(null, null)
            return Pair(cache.lastKnownLeft, cache.lastKnownRight)
        }

        /**
         * extracts shoulder data from compound tag.
         *
         * @param compoundTag The tag being checked
         * @return [ShoulderData] or null
         */
        @JvmStatic
        fun shoulderDataFrom(compoundTag: CompoundTag): ShoulderData? {
            if (compoundTag.isPokemonEntity()) {
                val uuid = this.extractUuid(compoundTag)
                return this.extractData(compoundTag, uuid)
            }
            return null
        }
    }
}