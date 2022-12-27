package tfc.tingedlights.api.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;
import tfc.tingedlights.LightBlender;
import tfc.tingedlights.MathUtils;
import tfc.tingedlights.data.Color;

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
			byte lightValue,
			BlockPos position,
			boolean distanceFade
	) {
		super(position, lightValue);
		this.color = color;
		this.endColor = endColor;
		this.blendThreshold = blendThreshold;
		this.distanceFade = distanceFade;
		
		colors = new Color[lightValue];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = calcColor((byte) i);
		}
	}
	
	protected Color calcColor(byte brightness) {
		int dist = brightness;
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
	
	public static Light of(Color color, int brightness, Vec3i position) {
		return new Light(color, color, 15, (byte) brightness, new BlockPos(position.getX(), position.getY(), position.getZ()), true);
	}
	
	public int distanceTo(BlockPos pPos) {
		float f = (float) Math.abs(position.getX() - pPos.getX());
		float f1 = (float) Math.abs(position.getY() - pPos.getY());
		float f2 = (float) Math.abs(position.getZ() - pPos.getZ());
		return (int) (f + f1 + f2);
	}
	
	@Override
	public int compareTo(@NotNull AbstractLight o) {
		if (o instanceof Light l) {
			int v = Byte.compare(lightValue, l.lightValue);
			if (v == 0) {
				v = color.compareTo(l.color);
				if (v == 0) {
					v = endColor.compareTo(l.endColor);
				}
			}
			return v;
		} else {
			return super.compareTo(o);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Light light = (Light) o;
		return blendThreshold == light.blendThreshold && lightValue == light.lightValue && distanceFade == light.distanceFade && Objects.equals(color, light.color) && Objects.equals(endColor, light.endColor) && Objects.equals(position, light.position);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(color, endColor, blendThreshold, lightValue, position, distanceFade);
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
	
	public byte lightValue() {
		return lightValue;
	}
	
	public BlockPos position() {
		return position;
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
				"lightValue=" + lightValue + ", " +
				"position=" + position + ", " +
				"distanceFade=" + distanceFade + ']';
	}
	
	@Override
	public Color getColor(byte brightness) {
		return colors[brightness - 1];
	}
}
