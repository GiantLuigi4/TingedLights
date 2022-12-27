package tfc.tingedlights.api.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;
import tfc.tingedlights.data.Color;

import java.util.Objects;

public record Light(
		Color color,
		Color endColor,
		int blendThreshold,
		byte lightValue,
		BlockPos position,
		boolean distanceFade
) implements Comparable<Light> {
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
	public int compareTo(@NotNull Light o) {
		int v = Byte.compare(lightValue, o.lightValue);
		if (v == 0) {
			v = color.compareTo(o.color);
			if (v == 0) {
				v = endColor.compareTo(o.endColor);
			}
		}
		return v;
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
}
