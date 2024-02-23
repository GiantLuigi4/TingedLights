package tfc.tingedlights.mixin.render.multibuffers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

@Mixin(MultiBufferSource.BufferSource.class)
public class BufferSourceMixin implements VertexBufferConsumerExtensions {
	Color defaultColor = new Color(0, 0, 0);
	
	@Override
	public void setDefault(Color color) {
		defaultColor = color;
	}
	
	@Override
	public Color getDefaultColor() {
		return defaultColor;
	}
	
	@Inject(at = @At("RETURN"), method = "getBuffer")
	public void postGetBuffer(RenderType pRenderType, CallbackInfoReturnable<VertexConsumer> cir) {
		VertexConsumer consumer = cir.getReturnValue();
		if (consumer instanceof VertexBufferConsumerExtensions extensions) {
			extensions.setDefault(defaultColor);
		}
	}
}
