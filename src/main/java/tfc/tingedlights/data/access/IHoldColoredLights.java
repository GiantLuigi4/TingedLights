package tfc.tingedlights.data.access;

import tfc.tingedlights.utils.LightInfo;

import java.util.Collection;

public interface IHoldColoredLights {
	Collection<LightInfo>[] getSources();
}
