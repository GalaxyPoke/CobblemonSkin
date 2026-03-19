/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.neoforge.mixin.client;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Unique
    boolean cobblemon$wasShiftClicking = false;

    /*
     * This pair of mixins resolves the fact that NeoForge specifically unsets the shift input if
     * the player is on a mount. I don't really understand why Chesterton's fence was there but I
     * assume it was to do with vanilla riding stuff and not Pokémon. I'll eat shit if I'm wrong ig.
     *
     * That code effectively meant that the ride code had no idea if shift is being clicked. The result?
     * Hover mounts had no way to descend. That's bad.
     *
     * Oh also the reason why I don't do HEAD for the pre is because the HEAD technically doesn't have
     * the input set yet. The (distant) superclass for LocalPlayer handling rideTick eventually is
     * responsible for putting the shift click value into the input so it's always false if we mixin
     * to the HEAD.
     *
     * - Hiro
     */

    @Inject(method = "rideTick()V", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/player/AbstractClientPlayer;rideTick()V"))
    public void rideTick$pre(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        cobblemon$wasShiftClicking = player.input.shiftKeyDown;
    }

    @Inject(method = "rideTick()V", at = @At(value = "TAIL"))
    public void rideTick$post(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (cobblemon$wasShiftClicking && player.getVehicle() instanceof PokemonEntity) {
            player.input.shiftKeyDown = cobblemon$wasShiftClicking;
        }
    }
}
