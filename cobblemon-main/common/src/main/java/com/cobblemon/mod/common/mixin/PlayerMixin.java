/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.OrientationControllable;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.item.LeftoversCreatedEvent;
import com.cobblemon.mod.common.api.orientation.OrientationController;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.riding.RidingStyle;
import com.cobblemon.mod.common.api.riding.behaviour.types.air.HoverBehaviour;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.tags.CobblemonItemTags;
import com.cobblemon.mod.common.duck.PlayerDuck;
import com.cobblemon.mod.common.duck.RidePassenger;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokedex.scanner.PokedexEntityData;
import com.cobblemon.mod.common.pokedex.scanner.ScannableEntity;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.CompoundTagExtensionsKt;
import com.cobblemon.mod.common.util.CompoundTagUtilities;
import com.cobblemon.mod.common.util.DataKeys;
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements ScannableEntity, OrientationControllable, PlayerDuck, RidePassenger {

    @Unique
    private Vector3f cobblemon$driverInput;
    @Unique
    private Vector3f cobblemon$lastSentDriverInput;

    @Shadow public abstract CompoundTag getShoulderEntityLeft();

    @Shadow public abstract CompoundTag getShoulderEntityRight();

    @Shadow public abstract void respawnEntityOnShoulder(CompoundTag entityNbt);

    @Shadow public abstract void setShoulderEntityRight(CompoundTag entityNbt);

    @Shadow public abstract void setShoulderEntityLeft(CompoundTag entityNbt);

    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract boolean addItem(ItemStack stack);

    @Shadow public abstract void displayClientMessage(Component message, boolean overlay);

    @Shadow @Final private static Map<Pose, EntityDimensions> POSES;
    @Shadow @Final public static EntityDimensions STANDING_DIMENSIONS;
    @Shadow protected int jumpTriggerTime;
    @Unique private final OrientationController cobblemon$orientationController = new OrientationController(this);

    @Unique private float cobblemon$rideXRot = 0.0f;

    @Unique private float cobblemon$rideYRot = 0.0f;

    @Unique private Vec3 cobblemon$rideEyePos = Vec3.ZERO;

    protected PlayerMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "respawnEntityOnShoulder", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"), cancellable = true)
    private void cobblemon$removePokemon(CompoundTag nbt, CallbackInfo ci) {
        if (CompoundTagExtensionsKt.isPokemonEntity(nbt)) {
            final UUID uuid = CompoundTagUtilities.getPokemonID(nbt);
            if (CompoundTagUtilities.isShoulderPokemon(this.getShoulderEntityRight())) {
                final UUID uuidRight = CompoundTagUtilities.getPokemonID(this.getShoulderEntityRight());
                if (uuid.equals(uuidRight)) {
                    this.recallPokemon(uuidRight);
                    this.setShoulderEntityRight(new CompoundTag());
                }
            }
            if (CompoundTagUtilities.isShoulderPokemon(this.getShoulderEntityLeft())) {
                final UUID uuidLeft = CompoundTagUtilities.getPokemonID(this.getShoulderEntityLeft());
                if (uuid.equals(uuidLeft)) {
                    this.recallPokemon(uuidLeft);
                    this.setShoulderEntityLeft(new CompoundTag());
                }
            }
            ci.cancel();
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        if (this.getVehicle() instanceof PokemonEntity) {
            return STANDING_DIMENSIONS;
        }
        return POSES.getOrDefault(pose, STANDING_DIMENSIONS);
    }

    @Inject(
        method = "removeEntitiesOnShoulder",
        at = @At(
            value = "JUMP",
            opcode = Opcodes.IFGE,
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void cobblemon$preventPokemonDropping(CallbackInfo ci) {
        // We want to allow both of these to forcefully remove the entities
        if (this.isSpectator() || this.isDeadOrDying())
            return;
        if (!CompoundTagUtilities.isShoulderPokemon(this.getShoulderEntityLeft())) {
            this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
            this.setShoulderEntityLeft(new CompoundTag());
        }
        if (!CompoundTagUtilities.isShoulderPokemon(this.getShoulderEntityRight())) {
            this.respawnEntityOnShoulder(this.getShoulderEntityRight());
            this.setShoulderEntityRight(new CompoundTag());
        }
        ci.cancel();
    }

    private void recallPokemon(UUID uuid) {
        // We need to do this cause the Entity doesn't store a reference to its storage
        final PartyStore party = Cobblemon.INSTANCE.getStorage().getParty(this.getUUID(), this.registryAccess());
        for (Pokemon pokemon : party) {
            if (pokemon.getUuid().equals(uuid)) {
                pokemon.recall();
            }
        }
    }

    @Redirect(
            method = "getDestroySpeed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"
            )
    )
    private boolean cobblemon$considerHoverOnGround(Player player) {
        // If riding a Hover Mount, return true to bypass the 5x slowdown
        if (player.getVehicle() instanceof PokemonEntity vehicle) {

            var ridingController = vehicle.getRidingController();
            if (ridingController == null) player.onGround();

            var activeContext = ridingController.getContext();
            if (activeContext == null) player.onGround();

            if (activeContext.getStyle() == RidingStyle.AIR &&
                activeContext.getBehaviour().equals(HoverBehaviour.Companion.getKEY())) {
                return true;
            }
        }
        // Otherwise, return the actual value
        return player.onGround();
    }

    @Inject(
        method = "eat",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getFoodData()Lnet/minecraft/world/food/FoodData;",
            shift = At.Shift.AFTER
        )
    )
    public void onEatFood(Level world, ItemStack stack, FoodProperties foodComponent, CallbackInfoReturnable<ItemStack> cir) {
        if (!level().isClientSide) {
            if (stack.is(CobblemonItemTags.LEAVES_LEFTOVERS) && level().random.nextDouble() < Cobblemon.config.getAppleLeftoversChance()) {
                ItemStack leftovers = new ItemStack(CobblemonItems.LEFTOVERS);
                ServerPlayer player = Objects.requireNonNull(getServer()).getPlayerList().getPlayer(uuid);
                assert player != null;
                CobblemonEvents.LEFTOVERS_CREATED.postThen(
                    new LeftoversCreatedEvent(player, leftovers),
                    leftoversCreatedEvent -> null,
                    leftoversCreatedEvent -> {
                        if(!player.addItem(leftoversCreatedEvent.getLeftovers())) {
                            var itemPos = player.getLookAngle().scale(0.5f).add(position());
                            level().addFreshEntity(new ItemEntity(level(), itemPos.x(), itemPos.y(), itemPos.z(), leftoversCreatedEvent.getLeftovers()));
                        }
                        return null;
                    }
                );
            }
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    public void isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> ci) {
        if (!level().isClientSide && (Object) this instanceof ServerPlayer player) {
            boolean invulnerableInBattle = this.level().getGameRules().getBoolean(CobblemonGameRules.BATTLE_INVULNERABILITY);
            boolean inBattle = Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null;
            if (invulnerableInBattle && inBattle) {
                ci.setReturnValue(true);
            }
        }
    }

    @Override @Nullable
    public PokedexEntityData resolvePokemonScan() {
        if (CompoundTagUtilities.isShoulderPokemon(this.getShoulderEntityRight())){
            return getDataFromShoulderPokemon(this.getShoulderEntityRight());
        }
        if (CompoundTagUtilities.isShoulderPokemon(this.getShoulderEntityLeft())){
            return getDataFromShoulderPokemon(this.getShoulderEntityLeft());
        }
        return null;
    }

    @Nullable @Unique
    private PokedexEntityData getDataFromShoulderPokemon(CompoundTag shoulderTag) {
        Pokemon pokemon;
        if (level().isClientSide) {
            CompoundTag pokemonTag = shoulderTag.getCompound(DataKeys.POKEMON);
            if (pokemonTag.isEmpty()) return null;
            Species species = PokemonSpecies.getByIdentifier(ResourceLocation.parse(pokemonTag.getString(DataKeys.POKEMON_SPECIES_IDENTIFIER)));
            if (species == null) return null;
            String formId = pokemonTag.getString(DataKeys.POKEMON_FORM_ID);
            FormData form = species.getStandardForm();
            List<FormData> formList = species.getForms().stream().filter(it -> it.formOnlyShowdownId().equals(formId)).toList();
            if (!formList.isEmpty()) form = formList.getFirst();
            if (form == null) return null;
            String genderString = pokemonTag.getString(DataKeys.POKEMON_GENDER);
            if (genderString.isEmpty()) return null;
            Gender gender = Gender.valueOf(genderString);
            boolean shiny = pokemonTag.getBoolean(DataKeys.POKEMON_SHINY);
            int level = pokemonTag.getInt(DataKeys.POKEMON_LEVEL);
            Set<String> aspects = shoulderTag.getList(DataKeys.SHOULDER_ASPECTS, Tag.TAG_STRING).stream().map(Tag::getAsString).collect(Collectors.toSet());

            pokemon = new Pokemon();
            pokemon.setSpecies(species);
            pokemon.setForm(form);
            pokemon.setGender(gender);
            pokemon.setShiny(shiny);
            pokemon.setLevel(level);
            pokemon.setForcedAspects(aspects);
        } else {
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(this.getUUID(), this.registryAccess());
            pokemon = party.get(shoulderTag.getUUID(DataKeys.SHOULDER_UUID));
        }
        return (pokemon == null) ? null : new PokedexEntityData(pokemon, null);
    }

    @Override
    public LivingEntity resolveEntityScan() {
        return this;
    }

    @Override
    public void absMoveTo(double x, double y, double z, float yaw, float pitch) {
        if (cobblemon$orientationController.getActive()) {
            this.absMoveTo(x, y, z);
            this.setYRot(yaw % 360.0f);
            this.setXRot(pitch % 360.0f);
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        else {
            this.absMoveTo(x, y, z);
            this.absRotateTo(yaw, pitch);
        }
    }

    @WrapOperation(
            method = "rideTick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;wantsToStopRiding()Z")
    )
    public boolean delegateDismountToController(Player instance, Operation<Boolean> original) {
        if (this.getVehicle() instanceof PokemonEntity pokemonEntity) {
            return pokemonEntity.ifRidingAvailableSupply(false, (behaviour, settings, state) -> {
                return behaviour.dismountOnShift(settings, state, pokemonEntity) && original.call(instance);
            });
        }
        return original.call(instance);
    }

    @Override
    public OrientationController getOrientationController() {
        return cobblemon$orientationController;
    }

    @Override
    public void setDriverInput(Vector3f driverInput) {
        cobblemon$driverInput = driverInput;
    }

    @Override
    public Vector3f getDriverInput() {
        return cobblemon$driverInput;
    }

    @Override
    public void setLastSentDriverInput(Vector3f lastSentDriverInput) {
        cobblemon$lastSentDriverInput = lastSentDriverInput;
    }

    @Override
    public Vector3f getLastSentDriverInput() {
        return cobblemon$lastSentDriverInput;
    }

    @Override
    public float cobblemon$getRideXRot() {
        return this.cobblemon$rideXRot;
    }

    @Override
    public void cobblemon$setRideXRot(float rideXRot) {
        this.cobblemon$rideXRot = Mth.wrapDegrees(rideXRot);
    }

    @Override
    public float cobblemon$getRideYRot() {
        return this.cobblemon$rideYRot;
    }

    @Override
    public void cobblemon$setRideYRot(float rideYRot) {
        this.cobblemon$rideYRot = Mth.wrapDegrees(rideYRot);
    }

    @Override
    public Vec3 cobblemon$getRideEyePos() {
        return this.cobblemon$rideEyePos;
    }

    @Override
    public void cobblemon$setRideEyePos(Vec3 rideEyePos) {
        this.cobblemon$rideEyePos = rideEyePos;
    }

}
