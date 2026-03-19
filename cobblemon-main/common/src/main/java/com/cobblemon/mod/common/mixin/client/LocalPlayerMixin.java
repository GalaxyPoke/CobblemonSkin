/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin.client;

import com.cobblemon.mod.common.CobblemonNetwork;
import com.cobblemon.mod.common.DoubleJump;
import com.cobblemon.mod.common.OrientationControllable;
import com.cobblemon.mod.common.api.orientation.OrientationController;
import com.cobblemon.mod.common.api.riding.Seat;
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.client.render.models.blockbench.FloatingState;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository;
import com.cobblemon.mod.common.duck.PlayerDuck;
import com.cobblemon.mod.common.duck.RidePassenger;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.server.orientation.ServerboundUpdateOrientationPacket;
import com.cobblemon.mod.common.net.messages.server.riding.ServerboundUpdateDriverInputPacket;
import com.cobblemon.mod.common.net.messages.server.riding.ServerboundUpdateRiderRotationPacket;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements OrientationControllable, DoubleJump {

    @Unique Matrix3f cobblemon$lastOrientation;

    @Unique float cobblemon$lastRideXRot;
    @Unique float cobblemon$lastRideYRot;
    @Unique Vec3  cobblemon$lastRideEyePos;

    @Unique boolean cobblemon$isDoubleJumping = false;

    @Shadow
    private float jumpRidingScale;

    @Shadow public Input input;

    @Shadow private int autoJumpTime;

    @Unique private int cobblemon$survivalJumpTriggerTime;

    @Unique private boolean cobblemon$isJumping;

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void cobblemon$updateRotationMatrixPassenger(CallbackInfo ci) {
        if (!(this.getVehicle() instanceof OrientationControllable controllableVehicle)) return;
        var vehicleController = controllableVehicle.getOrientationController();
        if (!vehicleController.isActive() || vehicleController.getOrientation() == cobblemon$lastOrientation) return;
        cobblemon$lastOrientation = vehicleController.getOrientation() != null ? new Matrix3f(vehicleController.getOrientation()) : null;
        CobblemonNetwork.INSTANCE.sendToServer(new ServerboundUpdateOrientationPacket(this.getVehicle().getId(), vehicleController.getOrientation()));
    }

    @Inject(method = "rideTick", at = @At("HEAD"))
    private void cobblemon$updateOrientationControllerRideTick(CallbackInfo ci) {
        if (Minecraft.getInstance().player != (Object)this) return;
        var driver = (LocalPlayer)(Object)this;
        var vehicle = driver.getVehicle();
        if (!(this.getVehicle() instanceof OrientationControllable controllableVehicle)) return;
        var shouldUseCustomOrientation = cobblemon$shouldUseCustomOrientation((LocalPlayer)(Object)this);
        var vehicleController = controllableVehicle.getOrientationController();

        // If the player has just switched to riding a custom orientation ride then set their
        // x and y rots local to the vehicle rots(if the controller wasn't active and will now be active)
        // This ensures that on transition your camera stays in the same spot
        if (!vehicleController.isActive() && shouldUseCustomOrientation) {
            // Set local to the vehicle x and yrot so
            var playerRotater = (RidePassenger)driver;
            playerRotater.cobblemon$setRideXRot(Mth.wrapDegrees(driver.getXRot() - vehicle.getXRot()));
            playerRotater.cobblemon$setRideYRot(Mth.wrapDegrees(driver.getYRot() - vehicle.getYRot()));
        }

        vehicleController.setActive(shouldUseCustomOrientation);
    }

    @Inject(method = "rideTick", at = @At("HEAD"))
    private void cobblemon$updateDriverInputRideTick(CallbackInfo ci) {
        if (Minecraft.getInstance().player != (Object)this) return;
        if (!(this.getVehicle() instanceof PokemonEntity pokemonEntity)) return;

        // Gather driver Input
        float vertInput = this.jumping ? 1.0f : (((Player)this).isShiftKeyDown() ? -1.0f : 0.0f);
        Vector3f driverInput = new Vector3f(Math.signum(this.xxa), vertInput, Math.signum(this.zza));

        // Check if player input has changed. If it has then update the last sent and send it
        Vector3f lastSentDriverInput = ((PlayerDuck)this).getLastSentDriverInput();
        if (driverInput.equals(lastSentDriverInput)) return;
        ((PlayerDuck)this).setLastSentDriverInput(driverInput);

        CobblemonNetwork.INSTANCE.sendToServer(new ServerboundUpdateDriverInputPacket(driverInput));
    }

    @Inject(method = "rideTick", at = @At("HEAD"))
    private void cobblemon$updateRiderRotationsRideTick(CallbackInfo ci) {
        if (Minecraft.getInstance().player != (Object)this) return;
        if (!(this.getVehicle() instanceof PokemonEntity pokemonEntity)) return;

        var passenger = (LocalPlayer)(Object)this;
        var playerRotater = (RidePassenger)passenger;

        // Gather Rider Rotation info
        var rideXRot = playerRotater.cobblemon$getRideXRot();
        var rideYRot = playerRotater.cobblemon$getRideYRot();
        var rideEyePos = playerRotater.cobblemon$getRideEyePos();

        // Check for a change in the values before sending them.
        if (cobblemon$lastRideXRot != rideXRot ||
            cobblemon$lastRideYRot != rideYRot ||
            cobblemon$lastRideEyePos != rideEyePos
            ) {

            // Update 'last' values
            cobblemon$lastRideXRot = rideXRot;
            cobblemon$lastRideYRot = rideYRot;
            cobblemon$lastRideEyePos = rideEyePos;

            CobblemonNetwork.INSTANCE.sendToServer(new ServerboundUpdateRiderRotationPacket(rideXRot, rideYRot, rideEyePos));
        }


    }

    @Unique
    private boolean cobblemon$shouldUseCustomOrientation(LocalPlayer player) {
        var playerVehicle = player.getVehicle();
        if (playerVehicle == null) return false;
        if (!(playerVehicle instanceof PokemonEntity pokemonEntity)) return false;
        return pokemonEntity.ifRidingAvailableSupply(false, (behaviour, settings, state) -> {
            return behaviour.shouldRoll(settings, state, pokemonEntity);
        });
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (Minecraft.getInstance().player != (Object)this) return;
        if (!(this instanceof OrientationControllable controllableVehicle)) return;
        controllableVehicle.getOrientationController().setActive(false);
    }

    @Inject(method = "getJumpRidingScale", at = @At("HEAD"), cancellable = true)
    public void modifyJumpRidingScale(CallbackInfoReturnable<Float> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (player.isPassenger() && player.getVehicle() instanceof PokemonEntity pokemonEntity) {
            var rideValue = pokemonEntity.<Float>ifRidingAvailableSupply(null, (behaviour, settings, state) -> {
                if (behaviour.canJump(settings, state, pokemonEntity, player)) return null;
                return behaviour.setRideBar(settings, state, pokemonEntity, player);
            });
            if (rideValue != null) {
                cir.setReturnValue(rideValue);
            }
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void cobblemon$aiStep(CallbackInfo ci) {
        this.cobblemon$isDoubleJumping = false;
        this.cobblemon$isJumping = this.input.jumping;
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSprinting()Z", shift = At.Shift.BEFORE))
    private void cobblemon$updateDoubleJumping(CallbackInfo ci) {
        if (!this.cobblemon$isJumping && this.input.jumping && this.autoJumpTime <= 0 && !this.isSwimming()) {
            if (this.cobblemon$survivalJumpTriggerTime != 0) {
                this.cobblemon$isDoubleJumping = true;
                this.cobblemon$survivalJumpTriggerTime = 0;
            }
            else {
                this.cobblemon$survivalJumpTriggerTime = 12;
            }
        }
        else if (this.cobblemon$survivalJumpTriggerTime > 0) {
            this.cobblemon$survivalJumpTriggerTime--;
        }
    }

    @Override
    public boolean isDoubleJumping() {
        return this.cobblemon$isDoubleJumping;
    }

    @Inject(method = "isHandsBusy", at = @At("HEAD"), cancellable = true)
    private void cobblemon$isHandsBusy(CallbackInfoReturnable<Boolean> cir) {
        Entity vehicle = this.getVehicle();
        if (vehicle instanceof PokemonEntity pokemonEntity) {
            int seatIndex = pokemonEntity.getPassengers().indexOf(this);
            Seat seat = pokemonEntity.getSeats().get(seatIndex);
            cir.setReturnValue(seat.getHandsBusy());
        }
    }

    @Override
    public HitResult pick(double hitDistance, float partialTicks, boolean hitFluids) {
        Entity vehicle = this.getVehicle();
        if (vehicle instanceof PokemonEntity pokemonEntity) {

            // Get player object
            var passenger = (LocalPlayer)(Object)this;
            var playerRotater = (RidePassenger)passenger;

            // Gather eye position from stored 1st person camera position calculation
            var eyePosition = playerRotater.cobblemon$getRideEyePos();

            Vec3 viewVector = this.getViewVector(partialTicks);
            Vec3 viewDistanceVector = eyePosition.add(viewVector.x * hitDistance, viewVector.y * hitDistance, viewVector.z * hitDistance);

            return this.level()
                    .clip(
                            new ClipContext(
                                    eyePosition, viewDistanceVector, ClipContext.Block.OUTLINE, hitFluids ? net.minecraft.world.level.ClipContext.Fluid.ANY : net.minecraft.world.level.ClipContext.Fluid.NONE, this
                            )
                    );
        }

        return super.pick(hitDistance, partialTicks, hitFluids);
    }

    @Unique
    private static @NotNull Vector3f cobblemon$getFirstPersonOffset(PosableModel model, String locatorName) {
        Map<String, Vec3> cameraOffsets = model.getFirstPersonCameraOffset();

        if (cameraOffsets.containsKey(locatorName)) {
            return cameraOffsets.get(locatorName).toVector3f();
        } else {
            return new Vector3f(0f, 0f, 0f);
        }
    }
}
