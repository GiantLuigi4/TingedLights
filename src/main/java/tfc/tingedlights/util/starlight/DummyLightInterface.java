package tfc.tingedlights.util.starlight;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;

public class DummyLightInterface {
	protected ArrayDeque<BlockStarLightEngine> cachedBlockPropagators;
	protected Level world;
	public LayerLightEventListener blockReader;
	
	public DummyLightInterface(LightChunkGetter lightAccess, boolean hasSkyLight, boolean hasBlockLight, LevelLightEngine lightEngine) {
	}
	
	public void releaseBlockLightEngine(BlockStarLightEngine engine) {
	}
	
	public int getSkyLightValue(BlockPos blockPos, ChunkAccess chunk) {
		return 0;
	}
	
	public int getBlockLightValue(BlockPos blockPos, ChunkAccess chunk) {
		return 0;
	}
	
	protected BlockStarLightEngine getBlockLightEngine() {
		return null;
	}
	
	public CompletableFuture<Void> blockChange(BlockPos pos) {
		return null;
	}
	
	public CompletableFuture<Void> sectionChange(SectionPos pos, boolean newEmptyValue) {
		return null;
	}
	
	public void propagateChanges() {
	}
}
