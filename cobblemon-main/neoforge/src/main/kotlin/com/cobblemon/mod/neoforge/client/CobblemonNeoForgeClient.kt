/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.client

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonClientImplementation
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonClient.pokedexUsageContext
import com.cobblemon.mod.common.client.CobblemonClient.reloadCodedAssets
import com.cobblemon.mod.common.client.gui.config.CobblemonConfigScreen
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds
import com.cobblemon.mod.common.client.pokedex.PokedexType
import com.cobblemon.mod.common.client.render.atlas.CobblemonAtlases
import com.cobblemon.mod.common.client.render.item.CobblemonModelPredicateRegistry
import com.cobblemon.mod.common.client.render.shader.CobblemonShaders
import com.cobblemon.mod.common.item.PokedexItem
import com.cobblemon.mod.common.particle.CobblemonParticles
import com.cobblemon.mod.common.particle.SnowstormParticleType
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.isUsingPokedex
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import net.minecraft.client.Minecraft
import net.minecraft.client.color.block.BlockColor
import net.minecraft.client.color.item.ItemColor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ReloadableResourceManager
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.ClientHooks
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent
import net.neoforged.neoforge.client.event.RegisterShadersEvent
import net.neoforged.neoforge.client.event.RenderGuiEvent
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.client.gui.VanillaGuiLayers
import net.neoforged.neoforge.common.NeoForge
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

object CobblemonNeoForgeClient : CobblemonClientImplementation {

    fun init() {
        with(MOD_BUS) {
            addListener(::onClientSetup)
            addListener(::onKeyMappingRegister)
            addListener(::onRegisterParticleProviders)
            addListener(::register3dModels)
            addListener(::onRegisterReloadListener)
            addListener(::onShaderRegistration)
        }
        with(NeoForge.EVENT_BUS) {
            addListener(::onRenderGuiOverlayEvent)
            addListener(::afterRenderGuiOverlayEvent)
            addListener(::afterEndClientTickEvent)
        }
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        event.enqueueWork {
            CobblemonClient.initialize(this)
            CobblemonModelPredicateRegistry.registerPredicates()
        }
        NeoForgeClientPlatformEventHandler.register()

        val modContainer = ModList.get().getModContainerById(Cobblemon.MODID).get()
        modContainer.registerExtensionPoint(IConfigScreenFactory::class.java, object : IConfigScreenFactory {
            override fun createScreen(modContainer: ModContainer, arg: Screen): Screen = CobblemonConfigScreen(arg)
        })
    }

    private fun onRegisterReloadListener(event: RegisterClientReloadListenersEvent) {
        event.registerReloadListener { synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor ->
            val atlasFutures = mutableListOf<CompletableFuture<Void>>()
            CobblemonAtlases.atlases.forEach {
                atlasFutures.add(
                    it.reload(
                        synchronizer,
                        manager,
                        prepareProfiler,
                        applyProfiler,
                        prepareExecutor,
                        applyExecutor
                    )
                )
            }
            val result = CompletableFuture.allOf(*atlasFutures.toTypedArray()).thenRun {
                reloadCodedAssets(manager)
            }
            result
        }

    }

    private fun onShaderRegistration(event: RegisterShadersEvent) {
        event.registerShader(ShaderInstance(event.resourceProvider, cobblemonResource("particle_add"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP)) {
            CobblemonShaders.PARTICLE_BLEND = it
        }
        event.registerShader(ShaderInstance(event.resourceProvider, cobblemonResource("particle_cutout"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP)) {
            CobblemonShaders.PARTICLE_CUTOUT = it
        }
    }

    override fun registerLayer(modelLayer: ModelLayerLocation, supplier: Supplier<LayerDefinition>) {
        ClientHooks.registerLayerDefinition(modelLayer, supplier)
    }

    override fun <T : ParticleOptions> registerParticleFactory(type: ParticleType<T>, factory: (SpriteSet) -> ParticleProvider<T>) {
        throw UnsupportedOperationException("Forge can't store these early, use CobblemonForgeClient#onRegisterParticleProviders")
    }

    @Suppress("DEPRECATION")
    override fun registerBlockRenderType(layer: RenderType, vararg blocks: Block) {
        blocks.forEach { block ->
            ItemBlockRenderTypes.setRenderLayer(block, layer)
        }
    }

    @Suppress("DEPRECATION")
    override fun registerItemColors(provider: ItemColor, vararg items: Item) {
        Minecraft.getInstance().itemColors.register(provider, *items)
    }

    @Suppress("DEPRECATION")
    override fun registerBlockColors(provider: BlockColor, vararg blocks: Block) {
        Minecraft.getInstance().blockColors.register(provider, *blocks)
    }

    override fun <T : BlockEntity> registerBlockEntityRenderer(type: BlockEntityType<out T>, factory: BlockEntityRendererProvider<T>) {
        BlockEntityRenderers.register(type, factory)
    }

    override fun <T : Entity> registerEntityRenderer(type: EntityType<out T>, factory: EntityRendererProvider<T>) {
        EntityRenderers.register(type, factory)
    }

    private fun register3dModels(event: ModelEvent.RegisterAdditional) {
        PokeBalls.all().forEach { pokeBall ->
            event.register(ModelResourceLocation(pokeBall.model3d, "standalone"))
        }
        PokedexType.entries.toList().forEach { pokedex ->
            event.register(ModelResourceLocation(pokedex.getItemModelPath(), "standalone"))
            event.register(ModelResourceLocation(pokedex.getItemModelPath("scanning"), "standalone"))
            event.register(ModelResourceLocation(pokedex.getItemModelPath("flat"), "standalone"))
            event.register(ModelResourceLocation(pokedex.getItemModelPath("flat_off"), "standalone"))
            event.register(ModelResourceLocation(pokedex.getItemModelPath("off"), "standalone"))
        }
        CobblemonItems.wearables.forEach { wearable ->
            event.register(ModelResourceLocation(wearable.getModel3d(),"standalone"))
        }
    }

    private fun onKeyMappingRegister(event: RegisterKeyMappingsEvent) {
        CobblemonKeyBinds.register(event::register)
    }

    private fun onRegisterParticleProviders(event: RegisterParticleProvidersEvent) {
        event.registerSpriteSet(CobblemonParticles.SNOWSTORM_PARTICLE_TYPE, SnowstormParticleType::Factory)
    }

    var lastUpdateTime: Long? = null

    private fun onRenderGuiOverlayEvent(event: RenderGuiLayerEvent.Pre) {
        if (event.name == VanillaGuiLayers.CHAT) {
            val lastUpdateTime = lastUpdateTime
            if (lastUpdateTime != null) {
                // "Why don't you just use the event.partialDeltaTicks"
                // Well JAMES it's because for some reason the value is like 2.8x too big. Forge bug? Weird event structure? Don't know don't care
                CobblemonClient.beforeChatRender(event.guiGraphics, (System.currentTimeMillis() - lastUpdateTime) / 1000F * 20F)
            }
            this.lastUpdateTime = System.currentTimeMillis()
        }
    }

    private fun afterRenderGuiOverlayEvent(event: RenderGuiEvent.Post) {
        val client = Minecraft.getInstance()
        val player = client.player
        if (player != null) {
            if (player.isUsingPokedex() || pokedexUsageContext.transitionIntervals > 0) {
                if (!player.isUsingItem) pokedexUsageContext.resetState(false)
                pokedexUsageContext.renderUpdate(event.guiGraphics, event.partialTick)
            } else {
                pokedexUsageContext.resetState()
            }
        }
    }

    private fun afterEndClientTickEvent(event: ClientTickEvent.Post) {
        val client = Minecraft.getInstance()
        val player = client.player
        if (player != null) {
            val itemStack = player.mainHandItem
            val offhandStack = player.offhandItem
            if (((itemStack.item is PokedexItem && player.usedItemHand == InteractionHand.MAIN_HAND) ||
                        (offhandStack.item is PokedexItem && player.usedItemHand == InteractionHand.OFF_HAND)) &&
                player.isUsingItem &&
                pokedexUsageContext.scanningGuiOpen
            ) {
                val keyAttack = client.options.keyAttack
                pokedexUsageContext.attackKeyHeld(keyAttack.isDown)
            }
        }
    }

    internal fun registerResourceReloader(reloader: PreparableReloadListener) {
        (Minecraft.getInstance().resourceManager as ReloadableResourceManager).registerReloadListener(reloader)
    }
}