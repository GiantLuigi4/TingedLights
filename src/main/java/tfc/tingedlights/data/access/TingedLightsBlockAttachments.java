package tfc.tingedlights.data.access;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.api.interfaces.QuadFunction;
import tfc.tingedlights.api.interfaces.TriFunction;

public interface TingedLightsBlockAttachments {
	Light createLight(BlockState pState, BlockGetter pLevel, BlockPos pPos);
	
	int getBrightness(BlockState pState, BlockGetter level, BlockPos pPos);
	
	boolean providesLight(BlockState pState, BlockGetter level, BlockPos pos);
	
	boolean needsUpdate(BlockState pState, BlockState pOld, BlockGetter pLevel, BlockPos pPos);
	
	void setFunctions(
			TriFunction<BlockState, BlockGetter, BlockPos, Light> lightCreation,
			TriFunction<BlockState, BlockGetter, BlockPos, Integer> brightnessGetter,
			TriFunction<BlockState, BlockGetter, BlockPos, Boolean> lightChecker,
			QuadFunction<BlockState, BlockState, BlockGetter, BlockPos, Boolean> updateChecker
	);
}
