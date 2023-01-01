package tfc.tingedlights;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.system.MemoryStack;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.ILightEngine;
import tfc.tingedlights.itf.VertexBufferConsumerExtensions;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class BlockTesselator {
	protected static final Color defaultColor = new Color(0, 0, 0);
	
	public static void putQuadData(BlockColors blockColors, BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, VertexConsumer pConsumer, PoseStack.Pose pPose, BakedQuad pQuad, float pBrightness0, float pBrightness1, float pBrightness2, float pBrightness3, int pLightmap0, int pLightmap1, int pLightmap2, int pLightmap3, int pPackedOverlay, ThreadLocal<BlockPos> posThreadLocal, boolean smooth, AOFace face) {
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
		int maxLightmap = 0;
		int maxBlock = 0;
		
		// TODO: maybe allow vanilla lighting on blocks that don't have colored lighting?
		for (int i = 0; i < lightmap.length; i++) {
			int unpacked = LightTexture.sky(lightmap[i]);
			maxLightmap = Math.max(maxLightmap, LightTexture.sky(lightmap[i]));
			maxBlock = Math.max(maxBlock, LightTexture.block(lightmap[i]));
//			lightmap[i] = LightTexture.pack(0, unpacked);
		}
		
		if (!(pConsumer instanceof VertexBufferConsumerExtensions)) {
			pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, 1, lightmap, pPackedOverlay, true);
			return;
		}
		
		VertexBufferConsumerExtensions extensions = ((VertexBufferConsumerExtensions) pConsumer);
		
		if (face != null) {
			Color[] colors = face.colors;
			boolean hasNonNull = false;
			for (int i = 0; i < colors.length; i++) {
				if (colors[i] == null) {
					colors[i] = new Color(0, 0, 0);
				} else {
					hasNonNull = true;
				}
			}
			if (!hasNonNull) {
				extensions.setColorDone(false);
				extensions.setDefault(new Color(0,0,0));
				pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, 1, lightmap, pPackedOverlay, true);
				extensions.setColorDone(false);
				return;
			}
			
			extensions.setColorDone(true);
			putBulkData(pConsumer, pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, lightmap, pPackedOverlay, true, new float[][]{
					new float[]{colors[0].r(), colors[0].g(), colors[0].b()},
					new float[]{colors[1].r(), colors[1].g(), colors[1].b()},
					new float[]{colors[2].r(), colors[2].g(), colors[2].b()},
					new float[]{colors[3].r(), colors[3].g(), colors[3].b()},
			});
			extensions.setColorDone(false);
		} else {
			BlockPos lightProbePos = posThreadLocal.get();
			if (lightProbePos == null) lightProbePos = pPos;
			LightManager manager = ((ILightEngine) pLevel.getLightEngine()).getManager();
			Color blockColor = manager.getColor(lightProbePos, true);
			if (blockColor == null) {
				extensions.setColorDone(false);
				extensions.setDefault(new Color(0, 0, 0));
				pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, 1, lightmap, pPackedOverlay, true);
				extensions.setColorDone(false);
				return;
			}
			
			extensions.setColorDone(true);
			putBulkData(pConsumer, pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, lightmap, pPackedOverlay, true, new float[]{blockColor.r(), blockColor.g(), blockColor.b()});
			extensions.setColorDone(false);
		}
	}
	
	protected static void putBulkData(VertexConsumer pConsumer, PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor, float[] pColor) {
		putBulkData(pConsumer, pPoseEntry, pQuad, pColorMuls, pRed, pGreen, pBlue, pCombinedLights, pCombinedOverlay, pMulColor, new float[][]{pColor, pColor, pColor, pColor});
	}
	
	protected static void putBulkData(VertexConsumer pConsumer, PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor, float[][] pColor) {
		float[] afloat = new float[]{pColorMuls[0], pColorMuls[1], pColorMuls[2], pColorMuls[3]};
		int[] aint1 = pQuad.getVertices();
		Vec3i vec3i = pQuad.getDirection().getNormal();
		Vector3f vector3f = new Vector3f((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ());
		Matrix4f matrix4f = pPoseEntry.pose();
		vector3f.transform(pPoseEntry.normal());
		int j = aint1.length / 8;
		MemoryStack memorystack = MemoryStack.stackPush();
		
		int firstVertex = 0;
		float maxV = 0;
		
		for (int k = 0; k < j; ++k) {
			float[] colors = pColor[k];
			float v = Math.max(colors[0], Math.max(colors[1], colors[2]));
			if (v > maxV) {
				maxV = v;
				firstVertex = k + 1;
			}
		}
		if (firstVertex >= j) firstVertex -= j;
		
		try {
			ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intbuffer = bytebuffer.asIntBuffer();
			
			boolean cont = true;
			for (int k = firstVertex; (k != firstVertex) || cont; k++) {
				cont = false;
				intbuffer.clear();
				intbuffer.put(aint1, k * 8, 8);
				float f = bytebuffer.getFloat(0);
				float f1 = bytebuffer.getFloat(4);
				float f2 = bytebuffer.getFloat(8);
				float f3;
				float f4;
				float f5;
				if (pMulColor) {
					float f6 = (float) (bytebuffer.get(12) & 255) / 255.0F;
					float f7 = (float) (bytebuffer.get(13) & 255) / 255.0F;
					float f8 = (float) (bytebuffer.get(14) & 255) / 255.0F;
					f3 = f6 * afloat[k] * pRed;
					f4 = f7 * afloat[k] * pGreen;
					f5 = f8 * afloat[k] * pBlue;
				} else {
					f3 = afloat[k] * pRed;
					f4 = afloat[k] * pGreen;
					f5 = afloat[k] * pBlue;
				}
				
				int l = pConsumer.applyBakedLighting(pCombinedLights[k], bytebuffer);
				float f9 = bytebuffer.getFloat(16);
				float f10 = bytebuffer.getFloat(20);
				Vector4f vector4f = new Vector4f(0, 0, 0, 0);
				vector4f.set(f, f1, f2, 1); // memory optimization: move vector declaration out of the loop
				vector4f.transform(matrix4f);
				pConsumer.applyBakedNormals(vector3f, bytebuffer, pPoseEntry.normal());
				vertex(pConsumer, vector4f.x(), vector4f.y(), vector4f.z(), f3, f4, f5, 1.0F, f9, f10, pCombinedOverlay, l, vector3f.x(), vector3f.y(), vector3f.z(), pColor[k]);
				
				if (k == (j - 1)) k = -1;
			}
		} catch (Throwable throwable1) {
			if (memorystack != null) {
				try {
					memorystack.close();
				} catch (Throwable throwable) {
					throwable1.addSuppressed(throwable);
				}
			}
			
			throw throwable1;
		}
		
		if (memorystack != null) {
			memorystack.close();
		}
	}
	
	protected static void vertex(VertexConsumer pConsumer, float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ, float[] lightColor) {
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
