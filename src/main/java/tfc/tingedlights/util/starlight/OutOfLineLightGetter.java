package tfc.tingedlights.util.starlight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface OutOfLineLightGetter {
	int TingedLights$getLight(BlockState state, BlockGetter level, BlockPos pos);
}
