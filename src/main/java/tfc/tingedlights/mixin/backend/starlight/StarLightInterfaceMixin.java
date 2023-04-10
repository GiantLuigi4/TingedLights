package tfc.tingedlights.mixin.backend.starlight;

import ca.spottedleaf.starlight.common.chunk.ExtendedChunk;
import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tfc.tingedlights.util.starlight.OutOfLineChunkExtensionAccessor;

@Mixin(value = StarLightInterface.class, remap = false)
public class StarLightInterfaceMixin implements OutOfLineChunkExtensionAccessor {
	@Redirect(method = "getBlockLightValue", at = @At(value = "INVOKE", target = "Lca/spottedleaf/starlight/common/chunk/ExtendedChunk;getBlockNibbles()[Lca/spottedleaf/starlight/common/light/SWMRNibbleArray;"))
	public SWMRNibbleArray[] preGetLightValue(ExtendedChunk instance) {
		return TingedLights$getBlockNibbles(instance);
	}
	
	@Override
	public SWMRNibbleArray[] TingedLights$getBlockNibbles(ExtendedChunk chunk) {
		return chunk.getBlockNibbles();
	}
}
