package tfc.tingedlights.data.struct;

import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;

public class LightBlock {
	public static final Color defaultColor = new Color(0, 0, 0);
	
	LightManager manager;
	
	protected Color color;
	protected int lightCount = 0;
	
	
	public Color getColor() {
		if (color == null) return defaultColor;
		return color;
	}
	
	public int lights() {
		return lightCount;
	}
	
	public void computeColor() {
		color = new Color(1, 1, 1);
	}
}
