package tfc.tingedlights.mixin.backend;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.api.interfaces.QuadFunction;
import tfc.tingedlights.api.interfaces.TriFunction;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

@Mixin(Block.class)
public class BlockMixin implements TingedLightsBlockAttachments {
	TriFunction<BlockState, Level, BlockPos, Light> lightCreation = (a, b, c) -> null;
	TriFunction<BlockState, Level, BlockPos, Boolean> lightChecker = (a, b, c) -> false;
	QuadFunction<BlockState, BlockState, Level, BlockPos, Boolean> updateChecker = (pState, pOld, pLevel, pPos) -> {
		if (pOld instanceof TingedLightsBlockAttachments yep)
			yep.providesLight(pOld, pLevel, pPos);
		return !pState.getBlock().equals(pOld.getBlock());
	};
	
	@Override
	public Light createLight(BlockState pState, Level pLevel, BlockPos pPos) {
		return lightCreation.accept(pState, pLevel, pPos);
	}
	
	/**
	 * the result of this method should not assume that an BlockEntity exists, nor that the block state in the world is the state being received by the method
	 * <p>
	 * if you do not have the information needed to figure out what this should return, assume it should return true
	 */
	@Override
	public boolean providesLight(BlockState pState, Level pLevel, BlockPos pPos) {
		return lightChecker.accept(pState, pLevel, pPos);
	}
	
	@Override
	public boolean needsUpdate(BlockState pState, BlockState pOld, Level pLevel, BlockPos pPos) {
		return updateChecker.accept(pState, pOld, pLevel, pPos);
	}
	
	@Override
	public void setFunctions(TriFunction<BlockState, Level, BlockPos, Light> lightCreation, TriFunction<BlockState, Level, BlockPos, Boolean> lightChecker, QuadFunction<BlockState, BlockState, Level, BlockPos, Boolean> updateChecker) {
		this.lightCreation = lightCreation == null ? this.lightCreation : lightCreation;
		this.lightChecker = lightChecker == null ? this.lightChecker : lightChecker;
		this.updateChecker = updateChecker == null ? this.updateChecker : updateChecker;
	}
}
