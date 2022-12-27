package tfc.tingedlights.data.access;

import net.minecraft.core.BlockPos;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.struct.LightNode;

import java.util.Collection;

public interface LevelWithColoredLightSupport {
	void addLight(Light light);
	void removeLight(Light light);
	
	Collection<LightNode> getSources(BlockPos pos);
	void removeLights(BlockPos pPos);
	void updateNeighbors(BlockPos pPos);
	
	LightManager getManager();
}
