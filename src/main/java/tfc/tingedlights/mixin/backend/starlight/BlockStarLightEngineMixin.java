package tfc.tingedlights.mixin.backend.starlight;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.tingedlights.util.starlight.OutOfLineLightGetter;

@Mixin(value = BlockStarLightEngine.class, remap = false)
public class BlockStarLightEngineMixin implements OutOfLineLightGetter {
	@Override
	public int TingedLights$getLight(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getLightEmission(level, pos);
	}
	
	@Redirect(method = "calculateLightValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
	public int getEmission0(BlockState instance, BlockGetter getter, BlockPos pos) {
		return TingedLights$getLight(instance, getter, pos);
	}
	
	@Redirect(method = "getSources", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
	public int getEmission1(BlockState instance, BlockGetter getter, BlockPos pos) {
		return TingedLights$getLight(instance, getter, pos);
	}
	
	@Redirect(method = "getSources", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
	public int getEmission2(BlockState instance) {
		return TingedLights$getLight(instance, null, null);
	}
	
	@Redirect(method = "lightChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
	public int getEmission3(BlockState instance, BlockGetter getter, BlockPos pos) {
		return TingedLights$getLight(instance, getter, pos);
	}
	
	@Redirect(method = "checkBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
	public int getEmission4(BlockState instance, BlockGetter getter, BlockPos pos) {
		return TingedLights$getLight(instance, getter, pos);
	}
}
