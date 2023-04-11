package tfc.tingedlights.utils.preprocessor;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import org.jetbrains.annotations.Nullable;
import tfc.tingedlights.utils.config.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DynamicLightPreprocessor extends GlslPreprocessor {
	GlslPreprocessor actualProcessor;
	String name;
	Program.Type type;
	
	public DynamicLightPreprocessor(GlslPreprocessor actualProcessor, String name, Program.Type type) {
		this.actualProcessor = actualProcessor;
		this.name = name;
		this.type = type;
	}
	
	@Nullable
	@Override
	public String applyImport(boolean pUseFullPath, String pDirectory) {
		return null;
	}
	
	private static final String injection;
	
	static {
		try {
			InputStream stream = DynamicLightPreprocessor.class.getClassLoader().getResourceAsStream("glsl/dynamic_light_sample.glsl");
			injection = new String(stream.readAllBytes());
			stream.close();
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	@Override
	public List<String> process(String pShaderData) {
		String injection = DynamicLightPreprocessor.injection;
		
		if (pShaderData.contains("\n//#define tinged_lights\n"))
			// TODO: if the file is not a dynamic lights file, it should add a define to indicate that dynamic lights are enabled
			return actualProcessor.process(pShaderData);
		if (pShaderData.contains("\n//#define tinged_lights:dynamic\n"))
			return actualProcessor.process(pShaderData);
		
		StringBuilder builder = new StringBuilder();
		
		int ln = 0;
		
		for (String s : pShaderData.split("\n")) {
			if (ln == 1) builder.append("uniform vec3 TingedLights_CameraOffset;\n");
			ln++;
			
			if (s.equals("in vec3 Position;")) injection = injection.replace("#ifdef Position", "#if 1");
			if (s.equals("in vec4 Position;")) injection = injection.replace("#ifdef Position", "#if 1");
			if (s.equals("uniform vec3 ChunkOffset;")) injection = injection.replace("#ifdef ChunkOffset", "#if 1");
			if (s.equals("uniform mat4 ModelViewMat;")) injection = injection.replace("#ifdef ModelViewMat", "#if 1");
			
			if (s.contains("minecraft_sample_lightmap(")) {
				s = s.replace("minecraft_sample_lightmap(",
						"\n" + injection
				);
			}
			builder.append(s).append("\n");
		}
		
		if (Config.GeneralOptions.dumpShaders) {
			try {
				String typeStr = type.getExtension();
				
				File file = new File("shader_dump/dynamic_lighting/core/" + name.replace(":", "/") + typeStr);
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
		
		return actualProcessor.process(builder.toString());
	}
}
