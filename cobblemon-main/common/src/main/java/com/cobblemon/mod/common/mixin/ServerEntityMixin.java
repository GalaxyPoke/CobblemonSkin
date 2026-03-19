/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.OrientationControllable;
import com.cobblemon.mod.common.api.net.NetworkPacket;
import com.cobblemon.mod.common.api.orientation.OrientationController;
import com.cobblemon.mod.common.duck.PlayerDuck;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.mixin.accessor.ChunkMapAccessor;
import com.cobblemon.mod.common.mixin.accessor.TrackedEntityAccessor;
import com.cobblemon.mod.common.net.messages.client.orientation.ClientboundUpdateDriverInputPacket;
import com.cobblemon.mod.common.net.messages.client.orientation.ClientboundUpdateOrientationPacket;
import com.cobblemon.mod.common.net.messages.client.pokemon.update.ClientboundUpdateRidingStatePacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {

    @Shadow
    @Final
    private Consumer<Packet<?>> broadcast;
    @Shadow
    @Final
    private Entity entity;
    @Unique
    private Matrix3f cobblemon$lastSentOrientation;
    @Unique
    private boolean cobblemon$lastSentActive;

    @Inject(method = "sendChanges", at = @At("TAIL"))
    private void cobblemon$sendChanges(CallbackInfo ci) {
        cobblemon$sendOrientationChanges();
        cobblemon$sendRidingStateChanges();
        cobblemon$sendDriverInput();
    }

    private void cobblemon$sendRidingStateChanges() {
        if (!(this.entity instanceof PokemonEntity pokemonEntity)) return;
        if (pokemonEntity.getRidingController() == null) return;
        var ridingController = pokemonEntity.getRidingController();
        if (ridingController.getContext() == null) return;
        var context = ridingController.getContext();
        var ridingBehaviour = context.getBehaviour();
        var ridingState = context.getState();
        var previousRidingState = pokemonEntity.getPreviousRidingState();
        if (previousRidingState != null && !ridingState.shouldSync(previousRidingState)) return;
        cobblemon$broadcast(new ClientboundUpdateRidingStatePacket(pokemonEntity.getId(), ridingBehaviour, ridingState, null));
    }

    private void cobblemon$sendOrientationChanges() {
        if (!(this.entity instanceof OrientationControllable controllable)) return;
        OrientationController controller = controllable.getOrientationController();
        Matrix3f currOrientation = controller.getOrientation();
        boolean currActive = controller.getActive();

        if (currOrientation == null || currOrientation.equals(cobblemon$lastSentOrientation) && (currActive == cobblemon$lastSentActive))
            return;
        cobblemon$lastSentOrientation = new Matrix3f(currOrientation);
        cobblemon$lastSentActive = currActive;

        cobblemon$broadcast(new ClientboundUpdateOrientationPacket(currOrientation, currActive, entity.getId()));
    }

    private void cobblemon$sendDriverInput() {
        if (!(this.entity instanceof ServerPlayer serverPlayer)) return;
        if(!(serverPlayer.getVehicle() instanceof PokemonEntity)) return;
        Vector3f driverInput = ((PlayerDuck)serverPlayer).getDriverInput();
        if(driverInput == null) return;

        // If no change in input is detected then don't send a new packet
        Vector3f lastSentDriverInput = ((PlayerDuck)serverPlayer).getLastSentDriverInput();
        if (driverInput.equals(lastSentDriverInput)) return;
        ((PlayerDuck)serverPlayer).setLastSentDriverInput(driverInput);
        cobblemon$broadcast(new ClientboundUpdateDriverInputPacket(driverInput, entity.getId()));
    }

    private void cobblemon$broadcast(NetworkPacket<?> packet) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        Int2ObjectMap<?> entityMap = ((ChunkMapAccessor) chunkMap).getEntityMap();
        Object tracked = entityMap.get(entity.getId());
        if (!(tracked instanceof TrackedEntityAccessor tracker)) return;

        Set<ServerPlayerConnection> seenBy = tracker.getSeenBy();
        for (ServerPlayerConnection conn : seenBy) {
            ServerPlayer player = conn.getPlayer();
            if (player == entity) continue;
            CobblemonNetwork.INSTANCE.sendPacketToPlayer(player, packet);
        }
    }

}
