package tfc.tingedlights.mixin.render.shaders;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.tingedlights.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

@Mixin(targets = "net.minecraft.client.renderer.ShaderInstance$1")
public class ShaderInstancePreprocessorMixin {
	@Inject(at = @At("RETURN"), method = "applyImport", cancellable = true)
	public void postApplyInput(boolean p_173374_, String p_173375_, CallbackInfoReturnable<String> cir) {
		if (p_173375_.equals("shaders/include/light.glsl")) {
			// TODO: do this better (count curlies to figure out where methods start and stop)
			String str = cir.getReturnValue();
			
			if (str.contains("\n//#define tinged_lights\n")) {
				cir.setReturnValue(str.replaceFirst("\n//#define tinged_lights\n", "\n#define tinged_lights\n"));
				return;
			}
			
			int targetMethod = str.indexOf("minecraft_sample_lightmap");
			String sub = str.substring(targetMethod);
			targetMethod += sub.indexOf("{");
			
			String out = str.substring(0, targetMethod + 1);
			out += "\n    #ifdef TINGEDLIGHTS_PATCHED\n" +
					"        vec4 light = vec4(LightColor, 1);\n" +
					"        uv = ivec2(15, uv.y);\n" +
					"        vec2 clamped = clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0));\n" +
					"        vec4 sky = texture(lightMap, clamped);\n" +
					"        \n" +
					"        // TODO: better blending for when light is less than sky\n" +
					"        if (tinged_lights_value(light) > tinged_lights_value(sky)) {\n" +
					"            sky += light;\n" +
					"        } else {\n" +
					"            if (tinged_lights_value(light) > tinged_lights_value(sky) - 0.15) {\n" +
					"                sky += light / 2;\n" +
					"            }\n" +
					"        }\n" +
					"        vec4 vec = max(sky, light);\n" +
					"        vec = min(vec, vec4(1));\n" +
					"        return vec;\n" +
					"    #else\n" +
					"    ";
			sub = str.substring(targetMethod + 1);
			if (sub.startsWith("\n")) sub = sub.substring(1);
			targetMethod = sub.indexOf("}");
			out += sub.substring(1, targetMethod);
			out += "    #endif\n" +
					"}";
			
			StringBuilder output = new StringBuilder();
			boolean inserted = false;
			for (String s : out.split("\n")) {
				output.append(s).append("\n");
				if (!inserted) {
					output.append("\n" + "float tinged_lights_value(vec4 color) {\n" + "    return max(color.r, max(color.b, color.g));\n" + "}\n");
					inserted = true;
				}
			}
			
			if (Options.dumpShaders) {
				if (!Options.wroteLightShader) {
					try {
						File file = new File("shader_dump/tinged_lights/include/light.glsl");
						if (!file.exists()) {
							file.getParentFile().mkdirs();
							file.createNewFile();
						}
						FileOutputStream outputStream = new FileOutputStream(file);
						outputStream.write(output.toString().getBytes(StandardCharsets.UTF_8));
						outputStream.flush();
						outputStream.close();
						Options.wroteLightShader = true;
					} catch (Throwable ignored) {
					}
				}
			}
			
			cir.setReturnValue(output.toString());
		}
	}
}
