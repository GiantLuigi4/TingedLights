package tfc.tingedlights;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.system.MemoryStack;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;
import tfc.tingedlights.utils.config.Config;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static tfc.tingedlights.utils.config.Config.TesselationOptions.VertexSortingOptions;

public class BlockTesselator {
	public static void putQuadData(BlockColors blockColors, BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, VertexConsumer pConsumer, PoseStack.Pose pPose, BakedQuad pQuad, float pBrightness0, float pBrightness1, float pBrightness2, float pBrightness3, int pLightmap0, int pLightmap1, int pLightmap2, int pLightmap3, int pPackedOverlay, boolean smooth, AOFace face, boolean repackLight) {
		TesselationState.guiLighting.set(false);
		
		float f;
		float f1;
		float f2;
		if (pQuad.isTinted()) {
			int i = blockColors.getColor(pState, pLevel, pPos, pQuad.getTintIndex());
			f = (float) (i >> 16 & 255) / 255.0F;
			f1 = (float) (i >> 8 & 255) / 255.0F;
			f2 = (float) (i & 255) / 255.0F;
		} else {
			f = 1.0F;
			f1 = 1.0F;
			f2 = 1.0F;
		}
		
		int[] lightmap = new int[]{pLightmap0, pLightmap1, pLightmap2, pLightmap3};
		
		// vanilla's AO algo is scuffed
		// so I added in an option to just replace it entirely
		if (Config.TesselationOptions.AOOptions.removeVanillaAO) {
			float directionalLighting =
					Config.TesselationOptions.directionalLighting ?
							pLevel.getShade(pQuad.getDirection(), pQuad.isShade()) :
							1;
			
			pBrightness0 = pBrightness1 = pBrightness2 = pBrightness3 = directionalLighting;
			
			if (face != null) {
				if (face.shades != null) {
					pBrightness0 *= face.shades[0];
					pBrightness1 *= face.shades[1];
					pBrightness2 *= face.shades[2];
					pBrightness3 *= face.shades[3];
				}
			}
		}
		
		if (!(pConsumer instanceof VertexBufferConsumerExtensions)) {
			putBulkData(pConsumer, pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, lightmap, pPackedOverlay, true, new float[]{0, 0, 0});
//			pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, 1, lightmap, pPackedOverlay, true);
			return;
		}
		
		// no
		// things in an if statement should not extend beyond the if's scope
		// I refuse to accept this syntax suggestion
		//noinspection PatternVariableCanBeUsed
		VertexBufferConsumerExtensions extensions = ((VertexBufferConsumerExtensions) pConsumer);
		
		if (face != null) {
			if (face.skylight != null) lightmap = face.skylight;
			
			Color[] colors = face.colors;
			boolean hasNonNull = false;
			for (int i = 0; i < colors.length; i++) {
				if (colors[i] == null) {
					colors[i] = Color.BLACK;
				} else {
					hasNonNull = true;
				}
			}
			if (!hasNonNull) {
				extensions.setColorDone(false);
				extensions.setDefault(Color.BLACK);
				putBulkData(pConsumer, pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, lightmap, pPackedOverlay, true, new float[]{0, 0, 0});
//				pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, 1, lightmap, pPackedOverlay, true);
				return;
			}
			
			extensions.setColorDone(true);
			putBulkData(pConsumer, pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, lightmap, pPackedOverlay, true, new float[][]{
					new float[]{colors[0].r(), colors[0].g(), colors[0].b()},
					new float[]{colors[1].r(), colors[1].g(), colors[1].b()},
					new float[]{colors[2].r(), colors[2].g(), colors[2].b()},
					new float[]{colors[3].r(), colors[3].g(), colors[3].b()},
			}, face.dimmed);
			extensions.setColorDone(false);
		} else {
			BlockPos lightProbePos = pPos;
			if (!repackLight)
				lightProbePos = lightProbePos.relative(pQuad.getDirection());
			
			LightManager manager = (LightManager) pLevel.getLightEngine();
			Color blockColor = manager.getColor(lightProbePos, true);
			if (blockColor == null) {
				extensions.setColorDone(false);
				extensions.setDefault(Color.BLACK);
				putBulkData(pConsumer, pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, lightmap, pPackedOverlay, true, new float[]{0, 0, 0});
//				pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, 1, lightmap, pPackedOverlay, true);
				return;
			}
			
			extensions.setColorDone(true);
			putBulkData(pConsumer, pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, lightmap, pPackedOverlay, true, new float[]{blockColor.r(), blockColor.g(), blockColor.b()});
			extensions.setColorDone(false);
		}
	}
	
	protected static final boolean[] defaultDimmed = new boolean[4];
	
	protected static void putBulkData(VertexConsumer pConsumer, PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor, float[] pColor) {
		putBulkData(pConsumer, pPoseEntry, pQuad, pColorMuls, pRed, pGreen, pBlue, pCombinedLights, pCombinedOverlay, pMulColor, new float[][]{pColor, pColor, pColor, pColor}, defaultDimmed);
	}
	
	protected static boolean aoSort() {
		int perp = VertexSortingOptions.SortingOptions.boxPerpendicular;
		int inne = VertexSortingOptions.SortingOptions.boxedInner;
		int oute = VertexSortingOptions.SortingOptions.boxOutside;
		return perp != 0 || inne != 0 || oute != 0;
	}
	
	protected static void putBulkData(VertexConsumer pConsumer, PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor, float[][] pColor, boolean[] pDimmed) {
		Matrix4f matrix4f = pPoseEntry.pose();
		
		Vec3i norm = pQuad.getDirection().getNormal();
		
		Vector3f bakedNormal = new Vector3f();
		
		int count = pQuad.getVertices().length / 8;
		Vector4f posVec = new Vector4f(0, 0, 0, 0);
		
		int firstVertex = 0;
		
		int countDimmed = 0;
		if (aoSort()) {
			for (boolean b : pDimmed) {
				if (b) {
					countDimmed++;
				}
			}
		}
		
		boolean skipVertSort = false;
		switch (countDimmed) {
			case 1 -> {
				switch (VertexSortingOptions.SortingOptions.boxOutside) {
					case 0 -> skipVertSort = false;
					case 1 -> {
						if (pDimmed[1]) firstVertex = 1;
						else if (pDimmed[3]) firstVertex = 1;
						skipVertSort = true;
					}
					case 2 -> {
						if (pDimmed[0]) firstVertex = 1;
						else if (pDimmed[2]) firstVertex = 1;
						skipVertSort = true;
					}
					case 3 -> skipVertSort = true;
				}
			}
			case 2 -> {
				boolean perpen = pDimmed[0] == pDimmed[2];
				
				if (perpen) {
					if (VertexSortingOptions.SortingOptions.boxPerpendicular != 0) {
						if (pDimmed[VertexSortingOptions.SortingOptions.boxPerpendicular])
							firstVertex = 1;
						skipVertSort = true;
					}
				}
			}
			case 3 -> {
				switch (VertexSortingOptions.SortingOptions.boxedInner) {
					case 0 -> skipVertSort = false;
					case 1 -> {
						if (!pDimmed[1]) firstVertex = 1;
						else if (!pDimmed[3]) firstVertex = 1;
						skipVertSort = true;
					}
					case 2 -> {
						if (!pDimmed[0]) firstVertex = 1;
						else if (!pDimmed[2]) firstVertex = 1;
						skipVertSort = true;
					}
				}
			}
		}
		
		if (!skipVertSort && Config.TesselationOptions.VertexSortingOptions.sortVertices) {
			float maxV = 0;
			
			// set the leading vertex to the brightest vertex to avoid jagged edges
			for (int k = 0; k < count; ++k) {
				float[] colors = pColor[k];
				float v = Math.max(colors[0], Math.max(colors[1], colors[2]));
				if (v > maxV) {
					maxV = v;
					firstVertex = k + 1;
				}
			}
			
			if (maxV == 0) {
				for (int k = 0; k < count; ++k) {
					float v = pCombinedLights[k];
					if (v > maxV) {
						maxV = v;
						firstVertex = k + 1;
					}
				}
			}
		}
		
		if (firstVertex >= count) firstVertex -= count;
		
		MemoryStack memorystack = MemoryStack.stackPush();
		try {
			ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intbuffer = bytebuffer.asIntBuffer();
			
			boolean cont = true;
			for (int k = firstVertex; (k != firstVertex) || cont; k++) {
				cont = false;
				
				// write the data into the buffer
				intbuffer.clear();
				intbuffer.put(pQuad.getVertices(), k * 8, 8);
				
				// position
				float x = bytebuffer.getFloat(0);
				float y = bytebuffer.getFloat(4);
				float z = bytebuffer.getFloat(8);
				
				// color
				float r;
				float g;
				float b;
				if (pMulColor) {
					float f6 = (float) (bytebuffer.get(12) & 255) / 255.0F;
					float f7 = (float) (bytebuffer.get(13) & 255) / 255.0F;
					float f8 = (float) (bytebuffer.get(14) & 255) / 255.0F;
					r = f6 * pColorMuls[k] * pRed;
					g = f7 * pColorMuls[k] * pGreen;
					b = f8 * pColorMuls[k] * pBlue;
				} else {
					r = pColorMuls[k] * pRed;
					g = pColorMuls[k] * pGreen;
					b = pColorMuls[k] * pBlue;
				}
				
				// lighting
				int light = applyBakedLighting(pCombinedLights[k], bytebuffer);
				
				// texture
				float u = bytebuffer.getFloat(16);
				float v = bytebuffer.getFloat(20);
				
				// position
				posVec.set(x, y, z, 1); // memory optimization: move vector declaration out of the loop
				posVec.transform(matrix4f);
				
				// normals
				if (applyBakedNormals(bakedNormal, bytebuffer, pPoseEntry.normal()))
					vertex(pConsumer, posVec.x(), posVec.y(), posVec.z(), r, g, b, 1.0F, u, v, pCombinedOverlay, light, bakedNormal.x(), bakedNormal.y(), bakedNormal.z(), pColor[k]);
				else {
					// this should never happen in block rendering
					Vector3f normalVec = new Vector3f((float) norm.getX(), (float) norm.getY(), (float) norm.getZ());
					normalVec.transform(pPoseEntry.normal());
					
					vertex(pConsumer, posVec.x(), posVec.y(), posVec.z(), r, g, b, 1.0F, u, v, pCombinedOverlay, light, normalVec.x(), normalVec.y(), normalVec.z(), pColor[k]);
				}
				
				if (k == (count - 1)) k = -1;
			}
		} catch (Throwable throwable1) {
			try {
				memorystack.close();
			} catch (Throwable throwable) {
				throwable1.addSuppressed(throwable);
			}
			
			throw throwable1;
		}
		
		memorystack.close();
	}
	
	// copy of forge code, ignores block light
	public static int applyBakedLighting(int packedLight, ByteBuffer data) {
		int sl = (packedLight >> 16) & 0xFFFF;
		int offset = LightUtil.getLightOffset(0) * 4; // int offset for vertex 0 * 4 bytes per int
		int slBaked = Short.toUnsignedInt(data.getShort(offset + 2));
		sl = Math.max(sl, slBaked);
		return (sl << 16);
	}
	
	// copy of forge code, allows a small memory optimization as well as preventing repeated matrix math
	public static boolean applyBakedNormals(Vector3f generated, ByteBuffer data, Matrix3f normalTransform) {
		byte nx = data.get(28);
		byte ny = data.get(29);
		byte nz = data.get(30);
		if (nx != 0 || ny != 0 || nz != 0) {
			generated.set(nx / 127f, ny / 127f, nz / 127f);
			generated.transform(normalTransform);
			return true;
		}
		return false;
	}
	
	public static void vertex(VertexConsumer pConsumer, float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ, float[] lightColor) {
		if (Config.TesselationOptions.fastDraw) {
			BufferBuilder builder = (BufferBuilder) pConsumer;
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
			
			int i = 24;
			// lightmap
			builder.putShort(i, (short) 0);
			builder.putShort(i + 2, (short) (pLightmapUV >> 16 & '\uffff'));
			// normal vector
			builder.putByte(i + 4, BufferVertexConsumer.normalIntValue(pNormalX));
			builder.putByte(i + 5, BufferVertexConsumer.normalIntValue(pNormalY));
			builder.putByte(i + 6, BufferVertexConsumer.normalIntValue(pNormalZ));
			// light color
			builder.putByte(i + 8, (byte) ((int) (lightColor[0] * 255.0F)));
			builder.putByte(i + 9, (byte) ((int) (lightColor[1] * 255.0F)));
			builder.putByte(i + 10, (byte) ((int) (lightColor[2] * 255.0F)));
			builder.putByte(i + 11, (byte) 255);
			// go to next vertex
			builder.nextElementByte += i + 12;
			builder.endVertex();
		} else {
			pConsumer.vertex(pX, pY, pZ);
			pConsumer.color(pRed, pGreen, pBlue, pAlpha);
			pConsumer.uv(pTexU, pTexV);
			pConsumer.overlayCoords(pOverlayUV);
			pConsumer.uv2(pLightmapUV);
			pConsumer.normal(pNormalX, pNormalY, pNormalZ);
			pConsumer.color((int) (lightColor[0] * 255), (int) (lightColor[1] * 255), (int) (lightColor[2] * 255), 255);
			pConsumer.endVertex();
		}
	}
}
