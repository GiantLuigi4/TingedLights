package tfc.tingedlights.data.access;

import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.struct.LightNode;
import tfc.tingedlights.data.struct.LightSource;

import java.util.Collection;

public interface IHoldColoredLights {
	Collection<Light>[] getSources();
}
