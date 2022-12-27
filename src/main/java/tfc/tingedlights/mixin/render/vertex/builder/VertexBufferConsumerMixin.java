package tfc.tingedlights.mixin.render.vertex.builder;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.TesselationState;
import tfc.tingedlights.VertexElements;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

import javax.annotation.Nullable;

@Mixin(BufferBuilder.class)
public abstract class VertexBufferConsumerMixin implements VertexBufferConsumerExtensions {
	@Unique
	boolean didColorAlready = false;
	
	@Unique
	Color defaultColor = new Color(0, 0, 0);
	
	@Override
	public void setDefault(Color color) {
		defaultColor = color;
	}
	
	@Override
	public Color getDefaultColor() {
		return TesselationState.getDefault(defaultColor);
	}
	
	@Override
	public void setColorDone(boolean val) {
		didColorAlready = val;
	}
	
	@Override
	public boolean isColorDone() {
		return didColorAlready;
	}
	
	@Shadow
	public abstract VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha);
	
	@Shadow
	@Nullable
	private VertexFormatElement currentElement;
	
	@Inject(at = @At("HEAD"), method = "endVertex")
	public void preEndVertex(CallbackInfo ci) {
		// TODO: do this better?
		if (!didColorAlready) {
			if (currentElement == VertexElements.ELEMENT_LIGHT_COLOR) {
				Color defaultColor = getDefaultColor();
				this.color((int) (defaultColor.r() * 255), (int) (defaultColor.g() * 255), (int) (defaultColor.b() * 255), 255);
			}
		}
	}
}
