package tfc.tingedlights.data;

import org.jetbrains.annotations.NotNull;

public record Color(float r, float g, float b) implements Comparable<Color> {
	public static final Color BLACK = new Color(0, 0, 0);
	
	public static Color fromRGB(int r, int g, int b) {
		return new Color(r / 255f, g / 255f, b / 255f);
	}
	
	public float getBrightness() {
		return Math.max(r, Math.max(g, b));
	}
	
	@Override
	public int compareTo(@NotNull Color o) {
		int v = Float.compare(getBrightness(), o.getBrightness());
		if (v == 0) {
			v = Float.compare(b, o.b);
			if (v == 0) {
				v = Float.compare(r, o.r);
				if (v == 0) {
					v = Float.compare(g, o.g);
				}
			}
		}
		return v;
	}
	
	public Color mul(double value) {
		return new Color(
				(float) (r * value),
				(float) (g * value),
				(float) (b * value)
		);
	}
}
