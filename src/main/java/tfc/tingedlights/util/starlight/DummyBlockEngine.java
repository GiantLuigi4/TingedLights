package tfc.tingedlights.util.starlight;

import ca.spottedleaf.starlight.common.chunk.ExtendedChunk;
import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LightChunkGetter;

import java.util.Iterator;
import java.util.Set;

public class DummyBlockEngine extends DummyStarEngine {
	public DummyBlockEngine(Level world) {
		super(world);
	}
	
	public boolean[] getEmptinessMap(ChunkAccess chunk) {
		return ((ExtendedChunk)chunk).getBlockEmptinessMap();
	}
	
	public void setEmptinessMap(ChunkAccess chunk, boolean[] to) {
		((ExtendedChunk)chunk).setBlockEmptinessMap(to);
	}
	
	public SWMRNibbleArray[] getNibblesOnChunk(ChunkAccess chunk) {
		return ((ExtendedChunk)chunk).getBlockNibbles();
	}
	
	public void setNibbles(ChunkAccess chunk, SWMRNibbleArray[] to) {
		((ExtendedChunk)chunk).setBlockNibbles(to);
	}
	
	public Iterator<BlockPos> getSources(LightChunkGetter lightAccess, ChunkAccess chunk) {
		return null;
	}
	
	public int getLightLevel(int worldX, int worldY, int worldZ) {
		return 0;
	}
	
	public BlockState getBlockState(int worldX, int worldY, int worldZ) {
		return null;
	}
	
	public void setLightLevel(int worldX, int worldY, int worldZ, int level) {
	}
	
	public void checkBlock(LightChunkGetter lightAccess, int worldX, int worldY, int worldZ) {
	}
	
	public void appendToIncreaseQueue(long value) {
	}
	
	public void appendToDecreaseQueue(long value) {
	}
	
	public void lightChunk(LightChunkGetter lightAccess, ChunkAccess chunk, boolean needsEdgeChecks) {
	}
	
	public void propagateBlockChanges(LightChunkGetter var1, ChunkAccess var2, Set<BlockPos> var3) {
	}
}