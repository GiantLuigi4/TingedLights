package tfc.tingedlights.api.data;

import tfc.tingedlights.data.Color;

public abstract class AbstractLight {
	@Override
	public abstract boolean equals(Object o);
	
	@Override
	public abstract int hashCode();
	
	public abstract Color getColor(byte brightness);
}
