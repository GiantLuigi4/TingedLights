package tfc.tingedlights.mixin.backend;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.BlockSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.IHoldColoredLights;
import tfc.tingedlights.data.access.LevelWithColoredLightSupport;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;"), method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", locals = LocalCapture.CAPTURE_FAILHARD)
	public void preSetBlockState(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir, LevelChunk levelchunk, Block block, BlockSnapshot blockSnapshot, BlockState old, int oldLight, int oldOpacity) {
		// TODO: deal with fluids
		if (this instanceof LevelWithColoredLightSupport lightSupport) {
			BlockState state = levelchunk.getBlockState(pPos);
			if (state.getBlock() instanceof TingedLightsBlockAttachments attachments) {
				Light light = attachments.createLight(state, (Level) (Object) this, pPos);
				if (light != null) {
					if (attachments.needsUpdate(pState, old, (Level) (Object) this, pPos)) {
						lightSupport.removeLight(light);
					}
				}
			}
		}
	}
	
	@Inject(at = @At(value = "RETURN", ordinal = 3), method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", locals = LocalCapture.CAPTURE_FAILHARD)
	public void postSetBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir, LevelChunk levelchunk, Block block, BlockSnapshot blockSnapshot, BlockState old, int oldLight, int oldOpacity, BlockState blockstate, BlockState blockstate1) {
		// TODO: deal with fluids
		if (cir.getReturnValue()) {
			if ((pFlags & 128) == 0) {
				Level level = (Level) (Object) this;
				if (this instanceof LevelWithColoredLightSupport lightSupport) {
					int lb = pState.getLightBlock(level, pPos);
					// TODO: get old state and see if it's a light source
					if (lb != 0) {
						lightSupport.removeLights(pPos);
					}
					
					boolean needsRecompute = true;
					if (old.getBlock() instanceof TingedLightsBlockAttachments potentialLightSource) {
						if (!potentialLightSource.needsUpdate(pState, old, level, pPos)) {
							needsRecompute = false;
						}
					}
					
					// TODO: optimize (do only when needed)
					if (pState.getBlock() instanceof TingedLightsBlockAttachments potentialLightSource) {
						Light light = potentialLightSource.createLight(pState, level, pPos);
						if (light != null) {
							lightSupport.addLight(light);
							if (levelchunk instanceof IHoldColoredLights iHoldColoredLights) {
								int sectionY = (int) SectionPos.blockToSection(light.position().getY());
								sectionY = levelchunk.getSectionIndex(sectionY);
								iHoldColoredLights.getSources()[sectionY].add(light);
							}
						} else if (needsRecompute) // TODO: may update light method?
							lightSupport.updateNeighbors(pPos);
					}
				}
			}
		}
	}
}
