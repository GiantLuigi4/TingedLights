package tfc.tingedlights.util.starlight;

import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import tfc.tingedlights.api.data.Light;

public interface ColorExtendedChunk {
	void setBlockNibbles(Light light, SWMRNibbleArray[] var1);
	SWMRNibbleArray[] getBlockNibbles(Light light);
	
	void setBlockEmptinessMap(Light light, boolean[] var1);
	boolean[] getBlockEmptinessMap(Light light);
	
	
	void setSkyNibbles(Light light, SWMRNibbleArray[] var1);
	SWMRNibbleArray[] getSkyNibbles(Light light);
	
	void setSkyEmptinessMap(Light light, boolean[] var1);
	boolean[] getSkyEmptinessMap(Light light);
}
