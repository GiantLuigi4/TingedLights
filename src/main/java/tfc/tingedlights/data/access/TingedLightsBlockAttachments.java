package tfc.tingedlights.data.access;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.api.interfaces.QuadFunction;
import tfc.tingedlights.api.interfaces.TriFunction;

public interface TingedLightsBlockAttachments {
	Light createLight(BlockState pState, Level pLevel, BlockPos pPos);
	boolean providesLight(BlockState pState, Level level, BlockPos pos);
	boolean needsUpdate(BlockState pState, BlockState pOld, Level pLevel, BlockPos pPos);
	
	void setFunctions(
			TriFunction<BlockState, Level, BlockPos, Light> lightCreation,
			TriFunction<BlockState, Level, BlockPos, Boolean> lightChecker,
			QuadFunction<BlockState, BlockState, Level, BlockPos, Boolean> updateChecker
	);
}
