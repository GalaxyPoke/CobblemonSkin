/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client.snapshots;

import com.cobblemon.mod.common.CobblemonBuildDetails;
import com.cobblemon.mod.common.client.gui.snapshots.SnapshotWarningScreen;
import com.cobblemon.mod.common.client.persisted.ClientPersistedData;
import kotlin.Unit;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(Minecraft.class)
public final class SnapshotWarningMixin {

    @Unique
    private final Pattern VERSION = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<patch>\\d+)$");

    @Inject(method = "addInitialScreens", at = @At("TAIL"))
    public void cobblemon$addSnapshotWarningScreen(List<Function<Runnable, Screen>> output, CallbackInfo callback) {
        if (CobblemonBuildDetails.SNAPSHOT && !this.cobblemon$isAcknowledged() && !SharedConstants.IS_RUNNING_IN_IDE) {
            output.add((runnable) -> new SnapshotWarningScreen((acknowledgement, dsa) -> {
                if (acknowledgement == SnapshotWarningScreen.Acknowledgement.NO) {
                    Minecraft.getInstance().stop();
                    return Unit.INSTANCE;
                }

                ClientPersistedData.INSTANCE.writeSnapshotsData(new ClientPersistedData.SnapshotAcknowledgementData(CobblemonBuildDetails.VERSION, dsa));

                runnable.run();
                return Unit.INSTANCE;
            }));
        }

    }

    @Unique
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean cobblemon$isAcknowledged() {
        ClientPersistedData.SnapshotAcknowledgementData data = ClientPersistedData.INSTANCE.readSnapshotsData();
        if (data == null) {
            return false;
        }

        Matcher current = VERSION.matcher(CobblemonBuildDetails.VERSION);
        current.find();

        Matcher when = VERSION.matcher(data.getVersion());
        when.find();

        boolean alert = current.group("minor").compareTo(when.group("minor")) > 0 || current.group("major").compareTo(when.group("major")) > 0;
        return !alert && data.getDontShowAgain();
    }

}
