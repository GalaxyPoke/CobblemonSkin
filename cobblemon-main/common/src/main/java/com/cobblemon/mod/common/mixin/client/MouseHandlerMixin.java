/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.OrientationControllable;
import com.cobblemon.mod.common.api.orientation.OrientationController;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.keybind.keybinds.PartySendBinding;
import com.cobblemon.mod.common.client.keybind.keybinds.RidingFreelookBinding;
import com.cobblemon.mod.common.duck.RidePassenger;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokedex.scanner.PokedexUsageContext;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.Blaze3D;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.cobblemon.mod.common.client.gui.party.PartyTutorialToasts;

import static com.google.common.primitives.Floats.min;
import static net.minecraft.util.Mth.lerp;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow private double accumulatedScrollY;
    @Shadow @Final private Minecraft minecraft;

    @Unique SmoothDouble xMouseSmoother = new SmoothDouble();
    @Unique SmoothDouble yMouseSmoother = new SmoothDouble();
    @Unique SmoothDouble pitchSmoother = new SmoothDouble();
    @Unique SmoothDouble rollSmoother = new SmoothDouble();
    @Unique SmoothDouble yawSmoother = new SmoothDouble();

    @Shadow @Final private SmoothDouble smoothTurnY;

    @Shadow @Final private SmoothDouble smoothTurnX;

    @Shadow private double accumulatedDX;

    @Shadow private double accumulatedDY;

    @Shadow private double lastHandleMovementTime;

    @Shadow public abstract boolean isMouseGrabbed();

    @Shadow protected abstract void turnPlayer(double movementTime);

    @Unique private double cobblemon$timeDelta;

    @Inject(
            method = "onScroll",
            at = @At(
                    value = "FIELD",
                    target="Lnet/minecraft/client/MouseHandler;accumulatedScrollY:D",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 2,
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    public void cobblemon$scrollParty(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (PartySendBinding.INSTANCE.getWasDown()) {
            int i = (int) accumulatedScrollY;
            if (i > 0) {
                accumulatedScrollY -= i;
                CobblemonClient.INSTANCE.getStorage().shiftSelected(false);
                ci.cancel();
                PartySendBinding.INSTANCE.actioned();
                PartyTutorialToasts.INSTANCE.onHoldKeyScrollSwitchedSlot();
            } else if (i < 0) {
                accumulatedScrollY -= i;
                CobblemonClient.INSTANCE.getStorage().shiftSelected(true);
                ci.cancel();
                PartySendBinding.INSTANCE.actioned();
                PartyTutorialToasts.INSTANCE.onHoldKeyScrollSwitchedSlot();
            }
        }
    }

    @Inject(
        method = "onScroll",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"
        ),
        cancellable = true
    )
    public void cobblemon$doPokedexZoom(long window, double horizontal, double vertical, CallbackInfo ci) {
        PokedexUsageContext usageContext = CobblemonClient.INSTANCE.getPokedexUsageContext();
        if (usageContext.getScanningGuiOpen()) {
            usageContext.adjustZoom(vertical);
            ci.cancel();
        }
    }

    @WrapWithCondition(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    public boolean cobblemon$modifyRotation(
        LocalPlayer player,
        double cursorDeltaX,
        double cursorDeltaY,
        @Local(argsOnly = true) double d,
        @Local(argsOnly = true) double movementTime
    ) {
        PokedexUsageContext usageContext = CobblemonClient.INSTANCE.getPokedexUsageContext();
        boolean returnValue = true;
        if (usageContext.getScanningGuiOpen()) {
            this.smoothTurnY.reset();
            this.smoothTurnX.reset();
            var defaultSensitivity = this.minecraft.options.sensitivity().get() * 0.6 + 0.2;
            var spyglassSensitivity = Math.pow(defaultSensitivity, 3);
            var lookSensitivity = spyglassSensitivity * 8.0;
            var sensitivity = lerp(usageContext.getFovMultiplier(), spyglassSensitivity, lookSensitivity);
            var yRotationFlip = this.minecraft.options.invertYMouse().get() ? -1 : 1;
            player.turn(this.accumulatedDX * sensitivity, (this.accumulatedDY * sensitivity * yRotationFlip));
            returnValue = false;
        }

        var vehicle = player.getVehicle();
        if (!(vehicle instanceof PokemonEntity)) return returnValue;

        // Clamp player rotation if riding and the vehicle demands it
        if (player.isPassenger() && vehicle instanceof PokemonEntity pokeVehicle) {
            pokeVehicle.clampPassengerRotation(player);
        }

        // If the ride is not orientation controllable then return and perform the normal turnPlayer logic
        if (!(vehicle instanceof OrientationControllable controllableVehicle) || !controllableVehicle.getOrientationController().isActive()) {
            xMouseSmoother.reset();
            yMouseSmoother.reset();
            pitchSmoother.reset();
            rollSmoother.reset();
            yawSmoother.reset();
            return returnValue;
        }

        var vehicleController = controllableVehicle.getOrientationController();
        var freeLooking = RidingFreelookBinding.INSTANCE.isDown();
        Pair<Boolean, Boolean> mouseShouldModifyDriver = ((PokemonEntity) vehicle).ifRidingAvailableSupply(new Pair(false, false), (behaviour, settings, state) -> {
            return behaviour.mouseModifiesDriverRotation(
                    settings,
                    state,
                    (PokemonEntity) vehicle
            );
        });

        //-------------------------------------------------------------------------
        // If looking around then modify the players orientation instead and zero the mouse deltas so the mouse doesn't
        // affect the vehicle orientation when looking around.
        // If the client is a passenger it will modify these rotations and return
        //-------------------------------------------------------------------------
        if (freeLooking || player.getControlledVehicle() == null) {
            // Modify driver rotations
            cobblemon$setDriverRotations(player, cursorDeltaX, cursorDeltaY);

            // If the player/passenger is not the driver then return and ignore all vehicle rotation logic
            if (player.getControlledVehicle() == null) return false;

            // Set to zero so that vehicle rotations don't also get affected by movement.
            cursorDeltaX = 0.0;
            cursorDeltaY = 0.0;
        } else if (mouseShouldModifyDriver.getFirst()) { // If not freelooking then check modification per mouse input dimension
            cobblemon$setDriverRotations(player, cursorDeltaX, 0.0);
            cursorDeltaX = 0.0;
        } else if (mouseShouldModifyDriver.getSecond()) {
            cobblemon$setDriverRotations(player, 0.0, cursorDeltaY);
            cursorDeltaY = 0.0;
        }

        //-------------------------------------------------------------------------
        // Smoothly return driver rotations to zero when needed
        //-------------------------------------------------------------------------
        var playerRotater = (RidePassenger)player;
        if( !freeLooking && !mouseShouldModifyDriver.getFirst()){
            playerRotater.cobblemon$setRideYRot(lerp(min(5.0f * (float)movementTime, 1.0f), Mth.wrapDegrees(playerRotater.cobblemon$getRideYRot()), 0.0f));
        }
        if( !freeLooking && !mouseShouldModifyDriver.getSecond()) {
            playerRotater.cobblemon$setRideXRot(lerp(min(5.0f * (float)movementTime, 1.0f), Mth.wrapDegrees(playerRotater.cobblemon$getRideXRot()), 0.0f));
        }

        // Send mouse input to be interpreted into rotation deltas by the ride controller
        Vec3 angVecMouse = cobblemon$getRideMouseRotation(cursorDeltaX, cursorDeltaY, movementTime);

        // Perform Rotation using mouse influenced rotation deltas.
        vehicleController.rotate(
            (float) angVecMouse.x,
            (float) angVecMouse.y,
            (float) angVecMouse.z
        );

        // Gather and apply the current rotation deltas
        var angRot = cobblemon$getAngularVelocity(movementTime);

        // Apply smoothing if requested by the controller.
        if(cobblemon$shouldUseAngVelSmoothing()) {
            var yaw = yawSmoother.getNewDeltaValue(angRot.x * 0.5f, d);
            var pitch = pitchSmoother.getNewDeltaValue(angRot.y * 0.5f, d);
            var roll = rollSmoother.getNewDeltaValue(angRot.z * 0.5f, d);
            vehicleController.rotate((float) yaw, (float) pitch, (float) roll);
        }
        else {
            vehicleController.rotate((float) (angRot.x * 10 * d), (float) (angRot.y * 10 * d), (float) (angRot.z * 10 * d));
        }

        return returnValue;
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD"))
    private void cobblemon$maintainMovementWhenInScreens2(CallbackInfo ci) {
        double time = Blaze3D.getTime();
        cobblemon$timeDelta = time - this.lastHandleMovementTime;
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("TAIL"))
    private void cobblemon$maintainMovementWhenInScreens(CallbackInfo ci) {
        if (minecraft.player == null) return;
        if (!(minecraft.player.getVehicle() instanceof OrientationControllable vehicleControllable)) return;
        if (!(vehicleControllable.getOrientationController() instanceof OrientationController vehicleController)) return;
        if (vehicleController.getOrientation() == null) return;
        if (minecraft.isPaused()) return;
        if (this.isMouseGrabbed()) return; // return if turnPlayer() has already run this pass

        this.turnPlayer(cobblemon$timeDelta);
    }

    @Unique
    private Vec3 cobblemon$getAngularVelocity(double deltaTime) {
        var player = minecraft.player;
        if (player == null) return Vec3.ZERO;
        if (!(player instanceof OrientationControllable)) return Vec3.ZERO;

        var playerVehicle = player.getVehicle();
        if (playerVehicle == null) return Vec3.ZERO;
        if (!(playerVehicle instanceof PokemonEntity pokemonEntity)) return Vec3.ZERO;
        return pokemonEntity.ifRidingAvailableSupply(Vec3.ZERO, (behaviour, settings, state) -> {
            return behaviour.angRollVel(settings, state, pokemonEntity, player, deltaTime);
        });
    }

    @Unique
    private Vec3 cobblemon$getRideMouseRotation(double mouseX, double mouseY, double deltaTime) {
        var player = minecraft.player;
        if (player == null) return Vec3.ZERO;
        var vehicle = player.getVehicle();
        if (vehicle == null) return Vec3.ZERO;
        if (!(vehicle instanceof PokemonEntity pokemonEntity)) return Vec3.ZERO;

        var invertYaw = Cobblemon.config.getInvertYaw();
        var invertPitch = Cobblemon.config.getInvertPitch();

        var xValue = mouseX * Cobblemon.config.getXAxisSensitivity();
        var yValue = mouseY * Cobblemon.config.getYAxisSensitivity();

        var swapXAndY = Cobblemon.config.getSwapXAndYAxes();

        var yaw = (swapXAndY ? yValue : xValue) * (invertYaw? -1.0f : 1.0f);

        var pitch = (swapXAndY ? xValue : yValue) * (invertPitch? -1.0f : 1.0f);

        var sensitivity = cobblemon$getRidingSensitivity();
        return pokemonEntity.ifRidingAvailableSupply(Vec3.ZERO, (behaviour, settings, state) -> {
            return behaviour.rotationOnMouseXY(
                    settings,
                    state,
                    pokemonEntity,
                    player,
                    pitch,
                    yaw,
                    yMouseSmoother,
                    xMouseSmoother,
                    sensitivity,
                    deltaTime
            );
        });
    }

    @Unique
    private double cobblemon$getRidingSensitivity() {
        var sensitivity = this.minecraft.options.sensitivity().get() * 0.6000000238418579 + 0.20000000298023224;
        return Math.pow(sensitivity, 3);
    }

    @Unique
    private void cobblemon$setDriverRotations(LocalPlayer player, double cursorDeltaX, double cursorDeltaY) {
        // the vanilla turn() method redone so that it uses rideRotations.
        float f = (float)cursorDeltaY * 0.15F; // Note: turn() uses xRot for pitch (up/down)
        float g = (float)cursorDeltaX * 0.15F; // Note: turn() uses yRot for yaw (left/right)
        var playerRotater = (RidePassenger)player;
        playerRotater.cobblemon$setRideXRot(playerRotater.cobblemon$getRideXRot() + f);
        playerRotater.cobblemon$setRideYRot(playerRotater.cobblemon$getRideYRot() + g);
        playerRotater.cobblemon$setRideXRot(Mth.clamp(playerRotater.cobblemon$getRideXRot(), -89.0F, 89.0F));
        playerRotater.cobblemon$setRideYRot(Mth.clamp(playerRotater.cobblemon$getRideYRot(), -105.0F, 105.0F));
        if (player.getVehicle() != null) {
            player.getVehicle().onPassengerTurned(player);
        }
    }

    @Unique
    private boolean cobblemon$shouldUseAngVelSmoothing() {
        var player = minecraft.player;
        if (player == null) return true;

        var playerVehicle = player.getVehicle();
        if (playerVehicle == null) return true;
        if (!(playerVehicle instanceof PokemonEntity pokemonEntity)) return true;
        return pokemonEntity.ifRidingAvailableSupply(true, (behaviour, settings, state) -> {
            return behaviour.useAngVelSmoothing(settings, state, pokemonEntity);
        });
    }

}
