package com.example.cobblemon_skin.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * 注入 PackRepository 构造函数，添加动态皮肤资源包源。
 * 仅在客户端环境生效。
 */
@Mixin(PackRepository.class)
public class PackRepositoryMixin {

    @Shadow @Final @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cobblemonSkin$addDynamicSkinSource(RepositorySource[] pSources, CallbackInfo ci) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                // Use reflection to avoid direct reference to client-only class on server
                Class<?> sourceClass = Class.forName("com.example.cobblemon_skin.client.DynamicSkinPackSource");
                RepositorySource source = (RepositorySource) sourceClass.getField("INSTANCE").get(null);
                Set<RepositorySource> mutable = new HashSet<>(this.sources);
                mutable.add(source);
                this.sources = mutable;
            } catch (Exception ignored) {
                // Silently ignore — may happen on dedicated server
            }
        }
    }
}
