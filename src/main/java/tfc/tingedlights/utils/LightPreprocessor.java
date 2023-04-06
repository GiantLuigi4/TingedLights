package tfc.tingedlights.utils;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import org.jetbrains.annotations.Nullable;
import tfc.tingedlights.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LightPreprocessor extends GlslPreprocessor {
	GlslPreprocessor actualProcessor;
	ParticlePreprocessor particlePreprocessor = null;
	String name;
	Program.Type type;
	
	public LightPreprocessor(GlslPreprocessor actualProcessor, String name, Program.Type type) {
		if (name.equals("particle")) {
			particlePreprocessor = new ParticlePreprocessor(type);
		}
		this.actualProcessor = actualProcessor;
		this.name = name;
		this.type = type;
	}
	
	@Nullable
	@Override
	public String applyImport(boolean pUseFullPath, String pDirectory) {
		return null;
	}
	
	@Override
	public List<String> process(String shaderFile) {
		if (shaderFile.contains("\n//#define tinged_lights\n")) { // find a better way?
			shaderFile = shaderFile.replaceFirst("\n//#define tinged_lights\n", "\n\n");
			return actualProcessor.process(shaderFile);
		}
		
		if (particlePreprocessor != null) shaderFile = particlePreprocessor.process(shaderFile);
		
		boolean containsLight = false;
		
		for (String line : shaderFile.split("\n")) {
			int len = line.length();
			line = line.replace("  ", " ");
			while (len != line.length()) {
				len = line.length();
				line = line
						.replace("  ", " ")
						.replace("( ", " ")
						.replace(" (", " ")
						.replace(" )", " ")
						.replace(") ", " ")
						.replace("\t", "")
						.trim();
			}
			
			if (line.startsWith("in ")) {
				break;
			}
			
			if (line.startsWith("#moj_import <light.glsl>")) {
				containsLight = true;
				break;
			}
		}
		
		if (containsLight) {
			StringBuilder output = new StringBuilder();
			for (String line : shaderFile.split("\n")) {
				if (line.startsWith("#moj_import <light.glsl>")) {
					continue;
				}
				if (containsLight) {
					if (line.startsWith("in ")) {
						output.append("#define TINGEDLIGHTS_PATCHED\n");
						output.append("in vec3 LightColor;\n");
						output.append("#moj_import <light.glsl>\n");
						containsLight = false;
					}
				}
				// TODO: I'd like to find a better way to target lightmap in entities
				String noSpace = line.replace(" ", "");
				if (noSpace.contains("=")) {
					String[] split = noSpace.split("=", 2);
					String right = split[1];
					if (right.contains("texelFetch(Sampler2, UV2 / 16, 0)".replace(" ", ""))) {
						line = noSpace.replace("texelFetch(Sampler2,UV2/16,0)", "minecraft_sample_lightmap(Sampler2, UV2)");
					}
				}
				output.append(line).append("\n");
			}
			
			if (Options.dumpShaders) {
				try {
					String typeStr = type.getExtension();
					
					File file = new File("shader_dump/tinged_lights/core/" + name.replace(":", "/") + typeStr);
					if (!file.exists()) {
						file.getParentFile().mkdirs();
						file.createNewFile();
					}
					FileOutputStream outputStream = new FileOutputStream(file);
					outputStream.write(output.toString().getBytes(StandardCharsets.UTF_8));
					outputStream.flush();
					outputStream.close();
				} catch (Throwable ignored) {
				}
			}
			
			return actualProcessor.process(output.toString());
		}
		return actualProcessor.process(shaderFile);
	}
}
