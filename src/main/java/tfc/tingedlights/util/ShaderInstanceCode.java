package tfc.tingedlights.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL13;
import tfc.tingedlights.api.DynamicLightApi;
import tfc.tingedlights.api.interfaces.TriFunction;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.utils.config.Config;

public class ShaderInstanceCode {
	public static void setupDynamicLights(ShaderInstance instance, int programId, TriFunction<String, Integer, Integer, Uniform> getOrMakeUniform) {
		GlStateManager._glUseProgram(programId);
		if (Config.GeneralOptions.useLightmap) {
			Uniform u = getOrMakeUniform.accept("TingedLights_LightTexture", 0, 1);
			if (u != null) {
				RenderSystem.activeTexture(GL13.GL_TEXTURE10);
				RenderSystem.enableTexture();
				RenderSystem.bindTexture(DynamicLightApi.getImg());
				u.set(10);
				u.upload();
			}
			Vector5f[] vecs = DynamicLightApi.getVecs();
			int len = vecs.length;
			for (int i = 0; i < 100; i++) {
				Vector5f lightVec = new Vector5f(0, 0, 0, -1, 0);
				if (i < len) lightVec = vecs[i];
				
				Uniform u1 = getOrMakeUniform.accept("TingedLights_LightCoords[" + i + "]", 7, 4);
				Uniform u2 = getOrMakeUniform.accept("TingedLights_LightColors[" + i + "]", 0, 1);
				if (u1 != null) {
					Vec3 vec = new Vec3(0, 0, 0);
					if (Minecraft.getInstance().player != null)
						vec = Minecraft.getInstance().player.getPosition(Minecraft.getInstance().getDeltaFrameTime());
					
					u1.set(
							(float) (lightVec.x - vec.x),
							(float) (lightVec.y - vec.y),
							(float) (lightVec.z - vec.z),
							(float) 15 - (lightVec.v)
					);
					u2.set((int) lightVec.w + 2);
					u1.upload();
					u2.upload();
				} else {
					break;
				}
			}
		} else {
			for (int i = 0; i < 15; i++) {
				Uniform u = getOrMakeUniform.accept("TingedLights_lightColors[" + i + "]", 6, 3);
				if (u != null) {
					Color c = DynamicLightApi.getColor(i);
					u.set(c.r(), c.g(), c.b());
					u.upload();
				} else {
					break;
				}
			}
		}
		Uniform u = getOrMakeUniform.accept("TingedLights_CameraOffset", 6, 3);
		if (u != null) {
			Vec3 cOffset = getCameraOffset(0); // TODO
			u.set((float) cOffset.x, (float) cOffset.y, (float) cOffset.z);
			u.upload();
		}
	}
	
	private static Vec3 getCameraOffset(float pct) {
		if (Minecraft.getInstance().cameraEntity != null) {
			Vec3 entityPos = Minecraft.getInstance().cameraEntity.getEyePosition(Minecraft.getInstance().getDeltaFrameTime());
			Vec3 cameraPos = Minecraft.getInstance().getEntityRenderDispatcher().camera.getPosition();
			
			return new Vec3(
					entityPos.x - cameraPos.x,
					entityPos.y - cameraPos.y,
					entityPos.z - cameraPos.z
			);
		} else {
			return new Vec3(0, 0, 0);
		}
	}
}
