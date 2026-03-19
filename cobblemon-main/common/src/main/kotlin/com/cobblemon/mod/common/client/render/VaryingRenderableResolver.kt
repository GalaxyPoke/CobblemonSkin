/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render

import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel
import com.cobblemon.mod.common.client.render.models.blockbench.PosableState
import com.cobblemon.mod.common.client.render.models.blockbench.pose.Bone
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository
import com.cobblemon.mod.common.util.adapters.*
import com.cobblemon.mod.common.util.asIdentifierDefaultingNamespace
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.resolveBoolean
import com.cobblemon.mod.common.util.toRGBA
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import kotlin.math.floor
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f
import org.joml.Vector4f

/**
 * All the information required for rendering a Pokémon/Poké Ball/NPC with aspects.
 *
 * @author Hiroku
 * @since May 14th, 2022
 */
class VaryingRenderableResolver(
    val name: ResourceLocation,
    val variations: MutableList<ModelAssetVariation>
) {
    lateinit var repository: VaryingModelRepository
    val posers = mutableMapOf<Pair<ResourceLocation, ResourceLocation>, PosableModel>()
    val models = mutableMapOf<ResourceLocation, Bone>()

    fun getResolvedPoser(state: PosableState): ResourceLocation {
        return getVariationValue(state) { poser }
            ?: throw IllegalStateException("Unable to find a poser for $name with aspects ${state.currentAspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    fun getResolvedModel(state: PosableState): ResourceLocation {
        return getVariationValue(state) { model }
            ?: throw IllegalStateException("Unable to find a model for $name with aspects ${state.currentAspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    fun getResolvedTexture(state: PosableState): ResourceLocation {
        return getVariationValue(state) { texture }?.invoke(state)
            ?: throw IllegalStateException("Unable to find a texture for $name with aspects ${state.currentAspects.joinToString()}. This shouldn't be possible if you've defined the fallback variation.")
    }

    fun getSprite(state: PosableState, type: SpriteType): ResourceLocation? {
        return getVariationValue(state) { sprites }?.get(type)
    }

    private fun <T> getVariationValue(state: PosableState, selector: ModelAssetVariation.() -> T?): T? {
        return variations.lastOrNull { it.fits(state) && selector(it) != null }?.let(selector)
    }

    fun getResolvedLayers(state: PosableState): Iterable<ModelLayer> {
        val layerMaps = mutableMapOf<String, ModelLayer>()
        for (variation in variations) {
            val layers = variation.layers
            if (layers != null && variation.fits(state)) {
                for (layer in layers) {
                    layerMaps[layer.name] = layer
                }
            }
        }
        return layerMaps.values.filter(ModelLayer::enabled)
    }

    fun getAllModels(): Set<ResourceLocation> {
        val models = mutableSetOf<ResourceLocation>()
        for (variation in variations) {
            if (variation.model != null) {
                models.add(variation.model)
            }
        }
        return models
    }

    companion object {
        val GSON = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
            .registerTypeAdapter(Vector3f::class.java, Vector3fAdapter)
            .registerTypeAdapter(Vector4f::class.java, Vector4fAdapter)
            .registerTypeAdapter(ModelTextureSupplier::class.java, ModelTextureSupplierAdapter)
            .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
            .registerTypeAdapter(SpriteType::class.java, SpriteTypeAdapter)
            .disableHtmlEscaping()
            .setLenient()
            .create()
    }

    fun initialize(repository: VaryingModelRepository) {
        this.repository = repository
        posers.clear()
        getAllModels().forEach { identifier ->
            try {
                models[identifier] = repository.texturedModels[identifier]!!
            } catch (e: Exception) {
                throw IllegalStateException("Unable to load model $identifier for $name", e)
            }
        }
    }

    fun getPoser(state: PosableState): PosableModel {
        val poserName = getResolvedPoser(state)
        val poserSupplier = repository.posers[poserName] ?: throw IllegalStateException("No poser found for name: $poserName for $name")
        val modelName = getResolvedModel(state)
        val existingEntityModel = posers[poserName to modelName]
        return if (existingEntityModel != null) {
            existingEntityModel
        } else {
            val model = models[modelName]!!
            val entityModel = poserSupplier(model)
            entityModel.initializeLocatorAccess()
            entityModel.registerPoses()
            posers[poserName to modelName] = entityModel
            entityModel
        }
    }

    fun getTexture(state: PosableState): ResourceLocation {
        repository.posers[getResolvedPoser(state)] ?: throw IllegalStateException("No poser for $name")
        return getResolvedTexture(state)
    }

    fun getLayers(state: PosableState): Iterable<ModelLayer> {
        repository.posers[getResolvedPoser(state)] ?: throw IllegalStateException("No poser for $name")
        return getResolvedLayers(state)
    }
}

/**
 * A set of variations. This is essentially a prioritized list of [ModelAssetVariation]s, with
 * an [order] property to control the priority of this set compared to other sets.
 *
 * @author Hiroku
 * @since December 4th, 2022
 */
class ModelVariationSet(
    @SerializedName("name", alternate = ["species", "pokeball"])
    val name: ResourceLocation = cobblemonResource("thing"),
    val order: Int = 0,
    val variations: MutableList<ModelAssetVariation> = mutableListOf()
)


/**
 * A variation to the base set, which can overwrite the poser, model, texture, or any combination of the above.
 * It contains a set of aspects that must ALL be present on a renderable for this variation to be considered.
 * If a later variation also matches, but provides different properties, both this and the other variation will
 * be used for their respective non-null properties.
 *
 * @author Hiroku
 * @since May 14th, 2022
 */
class ModelAssetVariation(
    val aspects: MutableSet<String> = mutableSetOf(),
    val condition: ExpressionLike? = null,
    val poser: ResourceLocation? = null,
    val model: ResourceLocation? = null,
    val texture: ModelTextureSupplier? = null,
    val layers: List<ModelLayer>? = null,
    val sprites: Map<SpriteType, ResourceLocation>? = null
) {
    fun fits(state: PosableState): Boolean {
        return aspects.all { it in state.currentAspects } && (condition == null || state.runtime.resolveBoolean(condition))
    }
}

/**
 * Given the animation seconds, returns a texture to use. Only implemented
 * by [StaticModelTextureSupplier] and [AnimatedModelTextureSupplier].
 *
 * @author Hiroku
 * @since February 6th, 2023
 */
fun interface ModelTextureSupplier {
    operator fun invoke(state: PosableState): ResourceLocation
}

class StaticModelTextureSupplier(val texture: ResourceLocation): ModelTextureSupplier {
    override fun invoke(state: PosableState): ResourceLocation {
        return texture
    }
}

class AnimatedModelTextureSupplier(
    val loop: Boolean,
    val fps: Float,
    val frames: List<ResourceLocation>,
    val interpolation: Boolean
): ModelTextureSupplier {
    override fun invoke(state: PosableState): ResourceLocation {
        val frameIndex = floor(state.animationSeconds * fps).toInt()
        return if (frameIndex >= frames.size && !loop) {
            frames.last()
        } else {
            frames[frameIndex % frames.size]
        }
    }

    fun interpolatedTexture(state: PosableState): DynamicTexture? {
        val resourceManager = Minecraft.getInstance().resourceManager

        val frameIndex = floor(state.animationSeconds * fps).toInt()
        try {
            if (frameIndex >= frames.size && !loop) {
                return DynamicTexture(NativeImage.read(resourceManager.getResourceOrThrow(frames.last()).open()))
            }
        } catch (e : Exception) {
            return null
        }
        val o = frameIndex % frames.size
        val i = state.animationSeconds * fps - frameIndex

        val texture1 : NativeImage
        val texture2 : NativeImage

        try { //Try getting images of the frames so we can use them to make a new one
            texture1 = NativeImage.read(resourceManager.getResourceOrThrow(frames[o]).open())
            val s = if (o == frames.size - 1) 0 else o + 1
            texture2 = NativeImage.read(resourceManager.getResourceOrThrow(frames[s]).open())
        } catch (e : Exception) {
            return null
        }

        for (x in 0..<texture1.width) {
            for (y in 0..<texture1.height) {
                val color1 = texture1.getPixelRGBA(x,y).toRGBA()
                val color2 = texture2.getPixelRGBA(x,y).toRGBA()

                var newRed : Int
                var newGreen : Int
                var newBlue : Int
                var newAlpha : Int

                //Usually full transparent pixel is black, so this makes it copy the other texture colours to look better, however considering if we should allow to disable this feature
                if (color1.w == 0F && color2.w == 1F) {
                    newRed = floor(color2.x * 255).toInt()
                    newGreen = floor(color2.y * 255).toInt()
                    newBlue = floor(color2.z * 255).toInt()
                } else if (color1.w == 1F && color2.w == 0F) {
                    newRed = floor(color1.x * 255).toInt()
                    newGreen = floor(color1.y * 255).toInt()
                    newBlue = floor(color1.z * 255).toInt()
                } else {
                    newRed = floor((color1.x + (color2.x - color1.x) * i) * 255).toInt()
                    newGreen = floor((color1.y + (color2.y - color1.y) * i) * 255).toInt()
                    newBlue = floor((color1.z + (color2.z - color1.z) * i) * 255).toInt()
                }

                newAlpha = floor((color1.w + (color2.w - color1.w) * i) * 255).toInt()

                val finalColor = (newAlpha shl 24) or (newRed shl 16) or (newGreen shl 8) or newBlue

                texture1.setPixelRGBA(x,y,finalColor) //We aren't using texture 1 anymore so it's easier to override it
            }
        }
        val newTexture = DynamicTexture(texture1)
        newTexture.setFilter(false, false)
        return newTexture
    }
}

class VariableModelTextureSupplier : ModelTextureSupplier {
    override fun invoke(state: PosableState): ResourceLocation {
        return state.runtime.environment.variable.map["texture"]?.asString()?.asIdentifierDefaultingNamespace()
            ?: cobblemonResource("textures/npcs/default.png")
    }
}

class ModelLayer {
    val name: String = ""
    val enabled: Boolean = true
    val tint: Vector4f = Vector4f(1F, 1F, 1F, 1F)
    val texture: ModelTextureSupplier? = null
    val emissive: Boolean = false
    val translucent: Boolean = false
    val translucent_cull: Boolean = false
    val scrolling: ScrollingTextureSettings? = null
}

class ScrollingTextureSettings(
    val speedU: Float = 0.01F,
    val speedV: Float = 0.01F
)

enum class SpriteType {
    PORTRAIT,
    PROFILE
}