package tfc.tingedlights.mixin.render.shaders;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.tingedlights.Options;
import tfc.tingedlights.utils.preprocessor.DynamicLightPreprocessor;
import tfc.tingedlights.utils.preprocessor.LightPreprocessor;

import java.io.InputStream;

@Mixin(Program.class)
public class ShaderProgramMixin {
	@Unique
	private static final ThreadLocal<String> name = new ThreadLocal<>();
	private static final ThreadLocal<Program.Type> type = new ThreadLocal<>();
	
	@Inject(at = @At("HEAD"), method = "compileShaderInternal")
	private static void preCompile(Program.Type pType, String pName, InputStream pShaderData, String pSourceName, GlslPreprocessor pPreprocessor, CallbackInfoReturnable<Integer> cir) {
		name.set(pName);
		type.set(pType);
	}
	
	@ModifyVariable(at = @At("HEAD"), method = "compileShaderInternal", ordinal = 0, argsOnly = true)
	private static GlslPreprocessor wrapProcessor(GlslPreprocessor glslPreprocessor) {
		if (Options.dynamicLights)
			glslPreprocessor = new DynamicLightPreprocessor(glslPreprocessor, name.get(), type.get());
		
		glslPreprocessor = new LightPreprocessor(glslPreprocessor, name.get(), type.get());
		
		name.remove();
		type.remove();
		return glslPreprocessor;
	}
}
