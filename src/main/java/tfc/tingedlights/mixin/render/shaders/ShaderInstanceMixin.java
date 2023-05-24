package tfc.tingedlights.mixin.render.shaders;

import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.util.ShaderInstanceCode;
import tfc.tingedlights.utils.config.Config;

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
		if (Config.GeneralOptions.dynamicLights)
			ShaderInstanceCode.setupDynamicLights((ShaderInstance) (Object) this, programId, this::getOrMakeUniform);
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
