package tfc.tingedlights.util.starlight;

import ca.spottedleaf.starlight.common.chunk.ExtendedChunk;
import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;

public interface OutOfLineChunkExtensionAccessor {
	SWMRNibbleArray[] TingedLights$getBlockNibbles(ExtendedChunk chunk);
}
