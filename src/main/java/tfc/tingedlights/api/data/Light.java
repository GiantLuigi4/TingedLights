package tfc.tingedlights.api.data;

import tfc.tingedlights.data.Color;

import java.util.Arrays;
import java.util.Objects;

public final class Light extends AbstractLight {
	private final Color color;
	private final Color endColor;
	private final int blendThreshold;
	private final boolean distanceFade;
	
	Color[] colors;
	
	public Light(
			Color color,
			Color endColor,
			int blendThreshold,
			boolean distanceFade
	) {
		this.color = color;
		this.endColor = endColor;
		this.blendThreshold = blendThreshold;
		this.distanceFade = distanceFade;
		
		colors = new Color[16];
		for (int i = 0; i < colors.length; i++)
			colors[i] = calcColor((byte) i);
		
		hc = calcHashCode();
	}
	
	public int calcHashCode() {
		int result = Objects.hash(color, endColor, blendThreshold, distanceFade);
		result = 31 * result + Arrays.hashCode(colors);
		return result;
	}
	
	protected Color calcColor(byte brightness) {
		int dist = brightness + 1;
		if (dist < 0) dist = 0;
		if (dist > 15) dist = 15;
		
		float d = dist / 15f;
//		final float divisor = 10;
//		final float mul = 1 / MathUtils.smoothLight(15 / divisor);
//		float d = MathUtils.smoothLight((dist) / divisor) * mul;
//		if (dist != 0) d = Mth.lerp(maxLightmap / 40f, d, 1);
//		d /= (node.getColor().r() + node.getColor().g() + node.getColor().b()) / 2;
		
		Color blended = color();
		// TODO: redo this?
		if (dist < blendThreshold()) {
			Color blendTo = endColor();
			
			float blendDist = ((dist - blendThreshold()));
			blendDist = -blendDist;
			blendDist /= (blendThreshold());
//				blendDist *= 15;
//				blendDist = MathUtils.smoothLight((blendDist) / divisor) * mul;
			blendDist = 1 - blendDist;
			blendDist = (float) Math.pow(blendDist, 2.5);
			blendDist = 1 - blendDist;
			
			blended = new Color(
					(blended.r() * (1 - blendDist)) + blendTo.r() * blendDist,
					(blended.g() * (1 - blendDist)) + blendTo.g() * blendDist,
					(blended.b() * (1 - blendDist)) + blendTo.b() * blendDist
			);
//			blended = new Color(
//					0, 0, blendDist
//			);

//			double b1 = blended.getBrightness();
//			double v = (Math.max(b0, b1) - Math.min(b0, b1)) / 1.2;
//			d += v;
//			if (d > 1) d = 1;
		}
		
		if (distanceFade()) {
			return new Color(
					blended.r() * d,
					blended.g() * d,
					blended.b() * d
			);
		} else {
			return blended;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		
		if (this.hc == o.hashCode()) {
			Light light = (Light) o;
			return blendThreshold == light.blendThreshold && distanceFade == light.distanceFade && Objects.equals(color, light.color) && Objects.equals(endColor, light.endColor) && Arrays.equals(colors, light.colors);
		} else return false;
	}
	
	public Color color() {
		return color;
	}
	
	public Color endColor() {
		return endColor;
	}
	
	public int blendThreshold() {
		return blendThreshold;
	}
	
	public boolean distanceFade() {
		return distanceFade;
	}
	
	@Override
	public String toString() {
		return "Light[" +
				"color=" + color + ", " +
				"endColor=" + endColor + ", " +
				"blendThreshold=" + blendThreshold + ", " +
				"distanceFade=" + distanceFade +
				']';
	}
	
	@Override
	public Color getColor(byte brightness) {
		return colors[brightness - 1];
	}
}
