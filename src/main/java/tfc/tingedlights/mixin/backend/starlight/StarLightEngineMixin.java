package tfc.tingedlights.mixin.backend.starlight;

import ca.spottedleaf.starlight.common.light.StarLightEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import tfc.tingedlights.util.asm.annotation.Hook;
import tfc.tingedlights.util.asm.annotation.MethodRedir;
import tfc.tingedlights.util.asm.annotation.RemoveMethods;
import tfc.tingedlights.util.asm.annotation.template.AnnotationTemplate;
import tfc.tingedlights.util.asm.annotation.template.MethodTarget;
import tfc.tingedlights.util.starlight.OutOfLineLightGetter;

@Mixin(StarLightEngine.class)
@Hook(StarLightEngine.class)
@RemoveMethods(
		targets = @MethodTarget(
				value = {"getEmission0", "getEmission1"},
				annotations = @AnnotationTemplate(
						type = "org.spongepowered.asm.mixin.transformer.meta.MixinMerged",
						values = {
								"mixin=tfc.tingedlights.mixin.backend.starlight.StarLightEngineMixin"
						}
				),
				matchAllAnnotations = false
		)
)
public class StarLightEngineMixin implements OutOfLineLightGetter {
	@Override
	public int TingedLights$getLight(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getLightEmission(level, pos);
	}
	
	@MethodRedir(
			exclude = @MethodTarget("TingedLights$getLight"),
			redirTarget = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission"
	)
	public int getEmission0(BlockState instance, BlockGetter getter, BlockPos pos) {
		return TingedLights$getLight(instance, getter, pos);
	}
	
	@MethodRedir(
			exclude = @MethodTarget("TingedLights$getLight"),
			redirTarget = {
					"Lnet/minecraft/world/level/block/state/BlockState;getLightEmission",
					"Lnet/minecraft/world/level/block/state/BlockState;m_60791_"
			}
	)
	public int getEmission1(BlockState instance) {
		return TingedLights$getLight(instance, null, null);
	}
}
