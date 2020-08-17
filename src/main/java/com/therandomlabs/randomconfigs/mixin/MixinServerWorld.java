package com.therandomlabs.randomconfigs.mixin;

import java.util.List;
import java.util.concurrent.Executor;

import com.therandomlabs.randomconfigs.api.event.world.CreateSpawnPositionCallback;
import com.therandomlabs.randomconfigs.api.event.world.WorldInitializeCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(
			MinecraftServer server, Executor workerExecutor, LevelStorage.Session session,
			ServerWorldProperties properties, RegistryKey<World> registryKey,
			DimensionType dimensionType, WorldGenerationProgressListener generationProgressListener,
			ChunkGenerator chunkGenerator, boolean bl, long l, List<Spawner> list, boolean bl2,
			CallbackInfo callback
	) {
		WorldInitializeCallback.EVENT.invoker().onInitialize((ServerWorld) (Object) this);
	}
}
