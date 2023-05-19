package tfc.tingedlights.util.starlight;

import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LightChunkGetter;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class ColoredStarlightEngine extends DummyBlockEngine implements OutOfLineLightGetter {
	Light type;
	BlockGetter level;
	LightChunkGetter chunkSource;
	
	@Override
	public boolean[] getEmptinessMap(ChunkAccess chunk) {
		return ((ColorExtendedChunk) chunk).getBlockEmptinessMap(type);
	}
	
	@Override
	public void setEmptinessMap(ChunkAccess chunk, boolean[] to) {
		((ColorExtendedChunk) chunk).setBlockEmptinessMap(type, to);
	}
	
	Pair<ChunkPos, SWMRNibbleArray[]> prev = null;
	
	@Override
	public SWMRNibbleArray[] getNibblesOnChunk(ChunkAccess chunk) {
		ChunkPos pos = chunk.getPos();
		
		Pair<ChunkPos, SWMRNibbleArray[]> prev = this.prev;
		
		SWMRNibbleArray[] nibble;
		if (prev == null || !prev.getFirst().equals(pos))
			this.prev = new Pair<>(pos, nibble = ((ColorExtendedChunk) chunk).getBlockNibbles(type));
		else nibble = prev.getSecond();
		
		return nibble;
	}
	
	@Override
	public void setNibbles(ChunkAccess chunk, SWMRNibbleArray[] to) {
		((ColorExtendedChunk) chunk).setBlockNibbles(type, to);
	}
	
	public ColoredStarlightEngine(Level world, Light type, LightChunkGetter chunkSource) {
		super(world);
		this.type = type;
		this.level = world;
		this.chunkSource = chunkSource;
	}
	
	@Override
	public int TingedLights$getLight(BlockState state, BlockGetter level, BlockPos pos) {
		Light light = ((TingedLightsBlockAttachments) state.getBlock()).createLight(state, level, pos);
		if (!type.equals(light)) return 0;
		return ((TingedLightsBlockAttachments) state.getBlock()).getBrightness(state, level, pos);
	}
	
	@Override
	public Iterator<BlockPos> getSources(LightChunkGetter lightAccess, ChunkAccess chunk) {
		Set<BlockPos> set = Collections.emptySet();
		return set.iterator();
	}
}
