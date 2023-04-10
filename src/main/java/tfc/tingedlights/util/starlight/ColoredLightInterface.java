package tfc.tingedlights.util.starlight;

import ca.spottedleaf.starlight.common.chunk.ExtendedChunk;
import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

public class ColoredLightInterface extends DummyLightInterface implements OutOfLineLightGetter, OutOfLineChunkExtensionAccessor {
	Light type;
	BlockGetter level;
	LightChunkGetter chunkSource;
	
	@Override
	public SWMRNibbleArray[] TingedLights$getBlockNibbles(ExtendedChunk chunk) {
		return ((ColorExtendedChunk) chunk).getBlockNibbles(type);
	}
	
	@Override
	public int TingedLights$getLight(BlockState state, BlockGetter level, BlockPos pos) {
		Light light = ((TingedLightsBlockAttachments) state.getBlock()).createLight(state, level, pos);
		if (!type.equals(light)) return 0;
		return ((TingedLightsBlockAttachments) state.getBlock()).getBrightness(state, level, pos);
	}
	
	public int getSkyLightValue(BlockPos blockPos, ChunkAccess chunk) {
		return 0;
	}
	
	public ColoredLightInterface(LevelLightEngine lightEngine, BlockGetter level, Light type, LightChunkGetter chunkSource) {
		super(chunkSource, false, true, lightEngine);
		this.type = type;
		this.level = level;
		this.chunkSource = chunkSource;
	}
	
	@Override
	public BlockStarLightEngine getBlockLightEngine() {
		if (this.cachedBlockPropagators == null) {
			return null;
		} else {
			BlockStarLightEngine ret;
			synchronized (this.cachedBlockPropagators) {
				ret = this.cachedBlockPropagators.pollFirst();
			}
			
			return ret == null ? (BlockStarLightEngine) (Object) new ColoredStarlightEngine(this.world, type, chunkSource) : ret;
		}
	}
	
	@Override
	public void releaseBlockLightEngine(BlockStarLightEngine engine) {
		if (this.cachedBlockPropagators != null) {
			synchronized (this.cachedBlockPropagators) {
				this.cachedBlockPropagators.addFirst(engine);
			}
		}
	}
	
	private final Deque<Pair<CompletableFuture<Void>, Runnable>> events = new ArrayDeque<>();
	
	@Override
	public void propagateChanges() {
		if (!events.isEmpty()) {
			synchronized (events) {
				ArrayList<Pair<CompletableFuture<Void>, Runnable>> runLater = new ArrayList<>();
				for (int i = 0; i < Minecraft.getInstance().options.renderDistance * Minecraft.getInstance().options.renderDistance; i++) {
					if (events.isEmpty()) break;
					
					Pair<CompletableFuture<Void>, Runnable> event = events.pop();
					if (event.getFirst().isCancelled()) continue;
					
					if (!event.getFirst().isDone()) runLater.add(event);
					else event.getSecond().run();
				}
				events.addAll(runLater);
			}
		}
		
		super.propagateChanges();
	}
	
	@Override
	public CompletableFuture<Void> sectionChange(SectionPos pPos, boolean newEmptyValue) {
		CompletableFuture<Void> s = super.sectionChange(pPos, newEmptyValue);
		
		if (!newEmptyValue) {
			synchronized (events) {
				events.add(Pair.of(s, () -> {
					BlockStarLightEngine engine = getBlockLightEngine();
					ShortArrayList list = new ShortArrayList();
					list.add((short) pPos.y());
					engine.checkChunkEdges(chunkSource, pPos.x(), pPos.z(), list);
					releaseBlockLightEngine(engine);
				}));
			}
		}
		
		return s;
	}
}
