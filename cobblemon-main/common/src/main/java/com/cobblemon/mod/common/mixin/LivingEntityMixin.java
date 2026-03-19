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
import com.cobblemon.mod.common.api.orientation.OrientationController;
import com.cobblemon.mod.common.api.pokemon.effect.ShoulderEffectRegistry;
import com.cobblemon.mod.common.api.riding.RidingStyle;
import com.cobblemon.mod.common.api.riding.Seat;
import com.cobblemon.mod.common.api.riding.behaviour.types.liquid.SubmarineBehaviour;
import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.net.messages.server.orientation.ServerboundUpdateOrientationPacket;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements OrientationControllable {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private final OrientationController cobblemon$orientationController = new OrientationController((LivingEntity) (Object) this);

    public OrientationController getOrientationController() {
        return cobblemon$orientationController;
    }

    @Inject(method = "onEffectRemoved", at = @At(value = "TAIL"))
    private void cobblemon$onEffectRemoved(MobEffectInstance effect, CallbackInfo ci) {
        final LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayer) {
            ShoulderEffectRegistry.INSTANCE.onEffectEnd((ServerPlayer) entity);
        }
    }

    @WrapOperation(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWall()Z"))
    public boolean cobblemon$preventMountSuffocation(LivingEntity instance, Operation<Boolean> original) {
        if (instance.getVehicle() instanceof PokemonEntity) {
            return false;
        }
        return original.call(instance);
    }

    @Override
    public @NotNull HitResult pick(double hitDistance, float partialTicks, boolean hitFluids) {
        Entity vehicle = this.getVehicle();
        if (vehicle instanceof PokemonEntity pokemonEntity && pokemonEntity.getDelegate() instanceof PokemonClientDelegate delegate) {
            int seatIndex = pokemonEntity.getPassengers().indexOf(this);
            Seat seat = pokemonEntity.getSeats().get(seatIndex);
            MatrixWrapper locator = delegate.getLocatorStates().get(seat.getLocator());

            Vec3 locatorOffset = new Vec3(locator.getMatrix().getTranslation(new Vector3f()));

            Vec3 eyePosition = cobblemon$getEyePosition(partialTicks, pokemonEntity, locatorOffset);

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
    private Vec3 cobblemon$getEyePosition(float partialTicks, PokemonEntity pokemonEntity, Vec3 locatorOffset) {
        OrientationController controller = this.getOrientationController();

        float currEyeHeight = this.getEyeHeight();
        Vec3 offset = locatorOffset.add(pokemonEntity.position());
        if (controller.isActive() && controller.getOrientation() != null) {
            Quaternionf orientation = controller.getRenderOrientation(partialTicks);
            Vec3 rotatedEyeHeight = new Vec3(orientation.transform(new Vector3f(0f, currEyeHeight - (this.getBbHeight() / 2), 0f)));

            offset.add(rotatedEyeHeight);
        }

        return offset;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void cobblemon$updateRenderOrientation(CallbackInfo ci) {
        this.cobblemon$orientationController.tick();
    }

    @Override
    public void absMoveTo(double x, double y, double z, float yaw, float pitch) {
        if (cobblemon$orientationController.getActive()) {
            this.absMoveTo(x, y, z);
            this.setYRot(yaw % 360.0f);
            this.setXRot(pitch % 360.0f);
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        } else {
            this.absMoveTo(x, y, z);
            this.absRotateTo(yaw, pitch);
        }
    }

    @Inject(method = "decreaseAirSupply", at = @At("HEAD"), cancellable = true)
    private void cobblemon$conditionalWaterBreathing(
            int currentAir,
            CallbackInfoReturnable<Integer> cir
    ) {
        var entity = (LivingEntity) (Object) this;
        if (!(entity instanceof Player player)) return;
        if (!(player.getVehicle() instanceof PokemonEntity vehicle)) return;

        var ridingController = vehicle.getRidingController();
        if (ridingController == null) return;

        var activeContext = ridingController.getContext();
        if (activeContext == null) return;

        // If the rider is currently on a submarine and the ride still has stamina.
        var stamina = activeContext.getState().getStamina().get();
        if (
                activeContext.getStyle() == RidingStyle.LIQUID &&
                activeContext.getBehaviour().equals(SubmarineBehaviour.Companion.getKEY()) &&
                stamina > 0.0f
        ) {
            cir.setReturnValue(currentAir);
            cir.cancel();
        }
    }

}
