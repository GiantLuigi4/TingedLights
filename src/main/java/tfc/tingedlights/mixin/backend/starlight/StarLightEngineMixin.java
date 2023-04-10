package tfc.tingedlights.mixin.backend.starlight;

import ca.spottedleaf.starlight.common.light.StarLightEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.tingedlights.util.starlight.OutOfLineLightGetter;

@Mixin(value = StarLightEngine.class, remap = false)
public class StarLightEngineMixin implements OutOfLineLightGetter {
	@Override
	public int TingedLights$getLight(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getLightEmission(level, pos);
	}
	
	@Redirect(method = "performLightDecrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
	public int getLightEmission0(BlockState instance) {
		return TingedLights$getLight(instance, null, null);
	}
	
	@Redirect(method = "performLightDecrease", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"))
	public int getLightEmission1(BlockState instance, BlockGetter getter, BlockPos pos) {
		return TingedLights$getLight(instance, getter, pos);
	}
}
