package tfc.tingedlights.mixin.backend.starlight;

import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import ca.spottedleaf.starlight.common.light.StarLightEngine;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.util.starlight.ColorExtendedChunk;

import java.util.HashMap;

@Mixin(ChunkAccess.class)
public class ChunkAccessMixin implements ColorExtendedChunk {
	@Shadow @Final protected LevelHeightAccessor levelHeightAccessor;
	@Unique
	HashMap<Light, SWMRNibbleArray[]> nibbles = new HashMap<>();
	@Unique
	HashMap<Light, boolean[]> emptiness = new HashMap<>();
	
	@Override
	public void setBlockEmptinessMap(Light light, boolean[] var1) {
		emptiness.put(light, var1);
	}
	
	@Override
	public boolean[] getBlockEmptinessMap(Light light) {
		return emptiness.get(light);
	}
	
	@Override
	public void setBlockNibbles(Light light, SWMRNibbleArray[] var1) {
		nibbles.put(light, var1);
	}
	
	@Override
	public SWMRNibbleArray[] getBlockNibbles(Light light) {
		SWMRNibbleArray[] nibble = nibbles.get(light);
		if (nibble == null) nibbles.put(light, nibble = StarLightEngine.getFilledEmptyLight((Level) levelHeightAccessor));
		return nibble;
	}
	
	@Override
	public SWMRNibbleArray[] getSkyNibbles(Light light) {
		throw new RuntimeException();
	}
	
	@Override
	public void setSkyNibbles(Light light, SWMRNibbleArray[] var1) {
		throw new RuntimeException();
	}
	
	@Override
	public boolean[] getSkyEmptinessMap(Light light) {
		throw new RuntimeException();
	}
	
	@Override
	public void setSkyEmptinessMap(Light light, boolean[] var1) {
		throw new RuntimeException();
	}
}
