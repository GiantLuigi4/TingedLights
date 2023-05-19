package tfc.tingedlights.mixin.backend.starlight;

import ca.spottedleaf.starlight.common.chunk.ExtendedChunk;
import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import org.spongepowered.asm.mixin.Mixin;
import tfc.tingedlights.util.asm.annotation.Hook;
import tfc.tingedlights.util.asm.annotation.MethodRedir;
import tfc.tingedlights.util.asm.annotation.RemoveMethods;
import tfc.tingedlights.util.asm.annotation.template.AnnotationTemplate;
import tfc.tingedlights.util.asm.annotation.template.MethodTarget;
import tfc.tingedlights.util.starlight.OutOfLineChunkExtensionAccessor;

@Mixin(StarLightInterface.class)
@Hook(StarLightInterface.class)
@RemoveMethods(
		targets = @MethodTarget(
				value = {"preGetLightValue"},
				annotations = @AnnotationTemplate(
						type = "org.spongepowered.asm.mixin.transformer.meta.MixinMerged",
						values = {
								"mixin=tfc.tingedlights.mixin.backend.starlight.StarLightInterfaceMixin"
						}
				),
				matchAllAnnotations = false
		)
)
public class StarLightInterfaceMixin implements OutOfLineChunkExtensionAccessor {
	@MethodRedir(
			exclude = @MethodTarget("TingedLights$getBlockNibbles"),
			redirTarget = "Lca/spottedleaf/starlight/common/chunk/ExtendedChunk;getBlockNibbles"
	)
	public final SWMRNibbleArray[] preGetLightValue(ExtendedChunk instance) {
		return TingedLights$getBlockNibbles(instance);
	}
	
	@Override
	public SWMRNibbleArray[] TingedLights$getBlockNibbles(ExtendedChunk chunk) {
		return chunk.getBlockNibbles();
	}
}
