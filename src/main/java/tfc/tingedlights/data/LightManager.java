package tfc.tingedlights.data;

import net.minecraft.core.BlockPos;
import tfc.tingedlights.api.data.Light;

public interface LightManager {
	void updateLight(Light light, BlockPos pos);
	
	Color getColor(BlockPos pos);
	
	Color getColor(BlockPos pos, boolean bl);
	
	void removeLights(BlockPos pPos);
	
	void updateNeighbors(BlockPos pPos);
	
	void enableEngine(Light light);
}
