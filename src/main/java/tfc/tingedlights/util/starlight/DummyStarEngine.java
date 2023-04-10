package tfc.tingedlights.util.starlight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class DummyStarEngine {
	public int coordinateOffset;
	public Level world;
	public BlockPos.MutableBlockPos checkBlockPos = new BlockPos.MutableBlockPos();
	
	public DummyStarEngine(Level world) {
		this.world = world;
	}
	
	public final void handleEmptySectionChanges(LightChunkGetter lightAccess, int chunkX, int chunkZ, Boolean[] emptinessChanges) {
	}
}