package com.therandomlabs.randomconfigs.mixin;

import java.util.concurrent.Executor;
import com.therandomlabs.randomconfigs.api.event.world.CreateSpawnPositionCallback;
import com.therandomlabs.randomconfigs.api.event.world.WorldInitializeCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(
			MinecraftServer server, Executor executor, WorldSaveHandler saveHandler,
			LevelProperties properties, DimensionType dimensionType, Profiler profiler,
			WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo callback
	) {
		WorldInitializeCallback.EVENT.invoker().onInitialize((ServerWorld) (Object) this);
	}

	@Inject(method = "init", at = @At("HEAD"))
	public void createSpawnPosition(LevelInfo info, CallbackInfo callback) {
		CreateSpawnPositionCallback.EVENT.invoker().onCreateSpawnPosition(
				(ServerWorld) (Object) this
		);
	}
}
