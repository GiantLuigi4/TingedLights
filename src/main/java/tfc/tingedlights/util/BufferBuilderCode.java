package tfc.tingedlights.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

public class BufferBuilderCode {
	public static boolean allowFast = true;
	
	public static boolean draw(BufferBuilder builder, boolean defaultColorSet, boolean fastFormat, boolean fullFormat, boolean mayNeedDefaults, float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
//		if (true) return true;
		if (defaultColorSet) {
			throw new IllegalStateException();
		} else if (fastFormat) {
			// position
			builder.putFloat(0, pX);
			builder.putFloat(4, pY);
			builder.putFloat(8, pZ);
			// tint color
			builder.putByte(12, (byte) ((int) (pRed * 255.0F)));
			builder.putByte(13, (byte) ((int) (pGreen * 255.0F)));
			builder.putByte(14, (byte) ((int) (pBlue * 255.0F)));
			builder.putByte(15, (byte) ((int) (pAlpha * 255.0F)));
			// texture
			builder.putFloat(16, pTexU);
			builder.putFloat(20, pTexV);
			int i;
			if (fullFormat) {
				builder.putShort(24, (short) (pOverlayUV & '\uffff'));
				builder.putShort(26, (short) (pOverlayUV >> 16 & '\uffff'));
				i = 28;
			} else {
				i = 24;
			}
			
			// lightmap
			builder.putShort(i, (short) (pLightmapUV & '\uffff'));
			builder.putShort(i + 2, (short) (pLightmapUV >> 16 & '\uffff'));
			// normal vector
			builder.putByte(i + 4, BufferVertexConsumer.normalIntValue(pNormalX));
			builder.putByte(i + 5, BufferVertexConsumer.normalIntValue(pNormalY));
			builder.putByte(i + 6, BufferVertexConsumer.normalIntValue(pNormalZ));
			
			if (mayNeedDefaults) {
				// light color
				Color defaultColor = ((VertexBufferConsumerExtensions) builder).getDefaultColor();
				float[] lightColor = new float[]{defaultColor.r(), defaultColor.g(), defaultColor.b()};
				builder.putByte(i +  8, (byte) ((int) (lightColor[0] * 255.0F)));
				builder.putByte(i +  9, (byte) ((int) (lightColor[1] * 255.0F)));
				builder.putByte(i + 10, (byte) ((int) (lightColor[2] * 255.0F)));
				builder.putByte(i + 11, (byte) 255);
				// continue
				builder.nextElementByte += i + 12;
			} else builder.nextElementByte += i + 8;
			
			builder.endVertex();
			
			return true;
		}
		return false;
	}
}
