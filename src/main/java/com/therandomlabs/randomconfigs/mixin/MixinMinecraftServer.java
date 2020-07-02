package com.therandomlabs.randomconfigs.mixin;

import com.therandomlabs.randomconfigs.api.event.world.CreateSpawnPositionCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
	@Inject(
			method = "setupSpawn",
			at = @At(
					value = "INVOKE",
					target = "net/minecraft/world/gen/chunk/ChunkGenerator.getBiomeSource()L" +
							"net/minecraft/world/biome/source/BiomeSource;"
			)
	)
	private static void setupSpawn(
			ServerWorld world, ServerWorldProperties properties, boolean flag1, boolean flag2,
			boolean flag3, CallbackInfo callback
	) {
		CreateSpawnPositionCallback.EVENT.invoker().onCreateSpawnPosition(world);
	}
}
