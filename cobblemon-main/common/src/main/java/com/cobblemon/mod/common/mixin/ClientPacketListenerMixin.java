/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Shadow private ClientLevel level;

    @Inject(method = "handleSetEntityPassengersPacket", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;hasIndirectPassenger(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void cobblemon$handlePokemonPassengers(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        Entity entity = this.level.getEntity(packet.getVehicle());
        if (entity instanceof PokemonEntity pokemon) {
            var passengers = new ArrayList<Entity>();
            for (int passenger : packet.getPassengers()) {
                passengers.add(this.level.getEntity(passenger));
            }
            Arrays
                    .stream(pokemon.getOccupiedSeats())
                    .filter(x -> !passengers.contains(x))
                    .filter(Objects::nonNull)
                    .forEach(Entity::stopRiding);

            var occupiedSeats = Arrays.asList(pokemon.getOccupiedSeats());
            passengers.stream()
                    .filter(x -> !occupiedSeats.contains(x))
                    .filter(Objects::nonNull)
                    .forEach(x -> x.startRiding(pokemon, true));
            ci.cancel();
        }
    }

}
