package tfc.tingedlights.data.struct;

import tfc.tingedlights.LightBlender;
import tfc.tingedlights.data.Color;

import java.util.Collection;

public class LightBlock {
	public static final Color defaultColor = new Color(0, 0, 0);
	
	protected Color color;
	protected Collection<LightNode> nodes;
	protected int lightCount = 0;
	
	public LightBlock(Collection<LightNode> nodes) {
		this.nodes = nodes;
	}
	
	public void addLight(LightNode node) {
		nodes.add(node);
		lightCount = -1;
	}
	
	public boolean removeLight(LightNode node) {
		lightCount = -1;
		return nodes.remove(node);
	}
	
	public Color getColor() {
		if (color == null) return defaultColor;
		return color;
	}
	
	public int lights() {
		if (lightCount == -1) return lightCount = nodes.size();
		return lightCount;
	}
	
	public boolean contains(LightNode lightNode) {
		return nodes.contains(lightNode);
	}
	
	public void computeColor() {
		color = LightBlender.blendLight(nodes);
	}
}
