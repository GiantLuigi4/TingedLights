package tfc.tingedlights.mixin.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VertexFormatElement.class)
public class VertexElementMixin {
	@Inject(at = @At("HEAD"), method = "supportsUsage", cancellable = true)
	public void preCheckSupportsUsage(int pIndex, VertexFormatElement.Usage pUsage, CallbackInfoReturnable<Boolean> cir) {
		// TODO: update a next index
		cir.setReturnValue(true);
	}
}
