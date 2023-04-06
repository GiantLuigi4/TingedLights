package tfc.tingedlights.utils;

import com.mojang.blaze3d.shaders.Program;
import tfc.tingedlights.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class ParticlePreprocessor {
	Program.Type type;
	
	public ParticlePreprocessor(Program.Type type) {
		this.type = type;
	}
	
	public String process(String pShaderData) {
		if (type.equals(Program.Type.FRAGMENT)) return pShaderData;
		
		StringBuilder builder = new StringBuilder();
		int ln = 0;
		for (String s : pShaderData.split("\n")) {
			ln++;
			
			if (ln == 2) builder.append("#moj_import <light.glsl>\n");
			if (s.trim().replace(" ", "").equals("#moj_import<light.glsl>")) continue;
			
			if (s.contains("texelFetch(Sampler2, UV2 / 16, 0)")) {// TODO: make this more bullet proof
				// honestly mojang should just do this
				// don't know why they don't
				s = s.replace("texelFetch(Sampler2, UV2 / 16, 0)", "minecraft_sample_lightmap(Sampler2, UV2)");
			}
			builder.append(s).append("\n");
		}
		
		if (Options.dumpShaders) {
			try {
				String typeStr = type.getExtension();
				
				File file = new File("shader_dump/tinged_lights/particle/core/particle" + typeStr);
				if (!file.exists()) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(builder.toString().getBytes(StandardCharsets.UTF_8));
				outputStream.flush();
				outputStream.close();
			} catch (Throwable ignored) {
			}
		}
		
		return builder.toString();
	}
}
