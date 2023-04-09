package tfc.tingedlights.mixin.render.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.Options;
import tfc.tingedlights.api.DynamicLightApi;
import tfc.tingedlights.data.Color;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin {
	@Shadow
	@Nullable
	public abstract Uniform getUniform(String pName);
	
	@Shadow
	@Final
	private List<Integer> uniformLocations;
	
	@Shadow
	@Final
	private Map<String, Uniform> uniformMap;
	
	@Shadow
	@Final
	private int programId;
	
	@Shadow
	@Final
	private BlendMode blend;
	
	@Inject(at = @At("TAIL"), method = "apply")
	public void preApply(CallbackInfo ci) {
		if (Options.dynamicLights) {
			GlStateManager._glUseProgram(programId);
			for (int i = 0; i < 15; i++) {
				Uniform u = getOrMakeUniform("TingedLights_lightColors[" + i + "]", 6, 3);
				if (u != null) {
					Color c = DynamicLightApi.getColor(i);
					u.set(c.r(), c.g(), c.b());
					u.upload();
				} else {
					break;
				}
			}
			Uniform u = getOrMakeUniform("TingedLights_CameraOffset", 6, 3);
			if (u != null) {
				Vec3 cOffset = getCameraOffset(0); // TODO
				u.set((float) cOffset.x, (float) cOffset.y, (float) cOffset.z);
				u.upload();
			}
		}
	}
	
	@Unique
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
	
	@Unique
	protected Uniform getOrMakeUniform(String name, int type, int count) {
		if (uniformMap.containsKey(name)) return uniformMap.get(name);
		
		Uniform uniform = new Uniform(name, type, count, (Shader) (Object) this);
		String s1 = uniform.getName();
		int k = Uniform.glGetUniformLocation(programId, s1);
		if (k != -1) {
			uniformLocations.add(k);
			uniform.setLocation(k);
			uniformMap.put(s1, uniform);
		} else {
			uniformMap.put(s1, null);
			return null;
		}
		return uniform;
	}
}
