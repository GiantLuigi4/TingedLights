package tfc.tingedlights.api.data;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import tfc.tingedlights.data.Color;

public abstract class AbstractLight implements Comparable<AbstractLight> {
	protected final BlockPos position;
	protected final byte lightValue;
	
	public AbstractLight(
			BlockPos position, byte lightValue
	) {
		this.position = position;
		this.lightValue = lightValue;
	}
	
	public int distanceTo(BlockPos pPos) {
		float f = (float) Math.abs(position.getX() - pPos.getX());
		float f1 = (float) Math.abs(position.getY() - pPos.getY());
		float f2 = (float) Math.abs(position.getZ() - pPos.getZ());
		return (int) (f + f1 + f2);
	}
	
	@Override
	public int compareTo(@NotNull AbstractLight o) {
		int v = Byte.compare(lightValue, o.lightValue);
		if (v == 0) {
			v = position.compareTo(o.position);
		}
		return v;
	}
	
	@Override
	public abstract boolean equals(Object o);
	
	@Override
	public abstract int hashCode();
	
	public BlockPos position() {
		return position;
	}
	
	public abstract Color getColor(byte brightness);
}
