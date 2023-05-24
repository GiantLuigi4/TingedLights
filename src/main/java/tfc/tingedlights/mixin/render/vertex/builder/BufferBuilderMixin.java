package tfc.tingedlights.mixin.render.vertex.builder;

import com.mojang.blaze3d.vertex.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.VertexElements;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;
import tfc.tingedlights.util.BufferBuilderCode;

import javax.annotation.Nullable;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin extends DefaultedVertexConsumer {
	@Shadow
	public abstract VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha);
	
	@Shadow
	private boolean fastFormat;
	
	@Shadow
	public abstract void putByte(int pIndex, byte pByteValue);
	
	@Shadow
	private int nextElementByte;
	
	@Shadow
	@Nullable
	private VertexFormatElement currentElement;
	
	@Shadow
	private VertexFormat format;
	
	@Shadow
	private int elementIndex;
	
	@Shadow
	public abstract void putFloat(int pIndex, float pFloatValue);
	
	@Shadow
	private boolean fullFormat;
	
	@Shadow
	public abstract void putShort(int pIndex, short pShortValue);
	
	@Shadow
	public abstract void endVertex();
	
	@Unique
	private boolean mayNeedDefaults = false;
	
	boolean allowFast = false;
	boolean usingFastFormat = fastFormat;
	
	@Inject(at = @At("TAIL"), method = "switchFormat")
	public void preSwitchFormat(VertexFormat pFormat, CallbackInfo ci) {
		mayNeedDefaults = false;
		allowFast = fastFormat;
		for (VertexFormatElement element : pFormat.getElements()) {
			if (element == VertexElements.ELEMENT_LIGHT_COLOR) {
				mayNeedDefaults = true;
				if (allowFast)
					fastFormat = BufferBuilderCode.allowFast; // TODO: fix&config
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "nextElement")
	public void preNextElement(CallbackInfo ci) {
		usingFastFormat = BufferBuilderCode.allowFast;
	}
	
	@Inject(at = @At("HEAD"), method = "endVertex")
	public void preEndVert(CallbackInfo ci) {
		if (!usingFastFormat) {
			if (currentElement == VertexElements.ELEMENT_LIGHT_COLOR) {
				if (!(((VertexBufferConsumerExtensions) this).isColorDone())) {
					Color defaultColor = ((VertexBufferConsumerExtensions) this).getDefaultColor();
					this.color((int) (defaultColor.r() * 255), (int) (defaultColor.g() * 255), (int) (defaultColor.b() * 255), 255);
				}
			}
			usingFastFormat = fastFormat;
		} else {
//			if (mayNeedDefaults) {
//				if (!(((VertexBufferConsumerExtensions) this).isColorDone())) {
//					Color defaultColor = ((VertexBufferConsumerExtensions) this).getDefaultColor();
//					this.putByte(0, (byte) ((int) (defaultColor.r() * 255.0F)));
//					this.putByte(1, (byte) ((int) (defaultColor.g() * 255.0F)));
//					this.putByte(2, (byte) ((int) (defaultColor.b() * 255.0F)));
//					this.putByte(3, (byte) 255);
//					nextElementByte += 4;
//				}
//			}
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/BufferVertexConsumer;color(IIII)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), method = "nextElement", cancellable = true)
	public void preDefault(CallbackInfo ci) {
		if (mayNeedDefaults) {
			if (currentElement == VertexElements.ELEMENT_LIGHT_COLOR) {
				if (!(((VertexBufferConsumerExtensions) this).isColorDone())) {
					Color defaultColor = ((VertexBufferConsumerExtensions) this).getDefaultColor();
					this.color((int) (defaultColor.r() * 255), (int) (defaultColor.g() * 255), (int) (defaultColor.b() * 255), 255);
				}
				ci.cancel();
			}
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "nextElement", cancellable = true)
	public void postNextElement(CallbackInfo ci) {
		if (mayNeedDefaults) {
			if (currentElement == VertexElements.ELEMENT_LIGHT_COLOR) {
				if (!(((VertexBufferConsumerExtensions) this).isColorDone())) {
					Color defaultColor = ((VertexBufferConsumerExtensions) this).getDefaultColor();
					this.color((int) (defaultColor.r() * 255), (int) (defaultColor.g() * 255), (int) (defaultColor.b() * 255), 255);
				}
				ci.cancel();
			}
		}
	}
	
	/**
	 * @author GiantLuigi4
	 */
	@Overwrite
	public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
		if (!BufferBuilderCode.draw((BufferBuilder) (Object) this, defaultColorSet, fastFormat, fullFormat, mayNeedDefaults, pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ))
			super.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
	}
}
