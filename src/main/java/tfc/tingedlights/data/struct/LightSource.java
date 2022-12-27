package tfc.tingedlights.data.struct;

import net.minecraft.core.BlockPos;
import tfc.tingedlights.api.data.Light;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record LightSource(
		LightNode reference,
		Set<LightNode> childNodes,
		Map<BlockPos, LightNode> system,
		Light light
) {
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LightSource that = (LightSource) o;
		return Objects.equals(light, that.light);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(light);
	}
}
