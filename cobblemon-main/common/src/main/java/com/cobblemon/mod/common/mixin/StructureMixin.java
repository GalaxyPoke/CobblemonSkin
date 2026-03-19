/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.mixin;

import com.cobblemon.mod.common.world.CobblemonStructureIDs;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(Structure.class)
public abstract class StructureMixin {
    // Define a set of structures that should not spawn below Sea Level
    @Unique
    private static final Set<ResourceLocation> RESTRICTED_STRUCTURES = new HashSet<>(Arrays.asList(
            CobblemonStructureIDs.STONJOURNER_HENGE,
            CobblemonStructureIDs.LUNA_HENGE,
            CobblemonStructureIDs.SOL_HENGE
    ));

    @Inject(method = "generate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/StructureStart;isValid()Z"),
            cancellable = true)
    public void cobblemon$isValid(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource,
                                  RandomState randomState, StructureTemplateManager structureTemplateManager, long seed,
                                  ChunkPos chunkPos, int references, LevelHeightAccessor heightAccessor,
                                  Predicate<Holder<Biome>> validBiome,
                                  CallbackInfoReturnable<StructureStart> cir,
                                  @Local StructureStart structureStart) {
        ResourceLocation structureKey = registryAccess.registryOrThrow(Registries.STRUCTURE).getKey((Structure) (Object) this);
        if(!RESTRICTED_STRUCTURES.contains(structureKey)) {
            return;
        }
        for (StructurePiece piece : structureStart.getPieces()) {
            if (piece.getBoundingBox().minY() < chunkGenerator.getSeaLevel()) {
                cir.setReturnValue(StructureStart.INVALID_START);
                return;
            }
        }
    }
}