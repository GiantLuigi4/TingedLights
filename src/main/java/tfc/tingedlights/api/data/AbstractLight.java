package tfc.tingedlights.api.data;

import tfc.tingedlights.data.Color;

public abstract class AbstractLight {
	protected int hc;
	
	@Override
	public abstract boolean equals(Object o);
	
	public abstract int calcHashCode();
	
	public abstract Color getColor(byte brightness);
	
	@Override
	public final int hashCode() {
		return hc;
	}
}
