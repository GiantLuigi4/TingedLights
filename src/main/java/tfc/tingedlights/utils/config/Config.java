package tfc.tingedlights.utils.config;

import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tfc.tingedlights.utils.config.annoconfg.AnnoCFG;
import tfc.tingedlights.utils.config.annoconfg.annotation.format.CFGSegment;
import tfc.tingedlights.utils.config.annoconfg.annotation.format.Comment;
import tfc.tingedlights.utils.config.annoconfg.annotation.format.Name;
import tfc.tingedlights.utils.config.annoconfg.annotation.format.Skip;
import tfc.tingedlights.utils.config.annoconfg.annotation.value.Default;
import tfc.tingedlights.utils.config.annoconfg.annotation.value.FloatRange;

@tfc.tingedlights.utils.config.annoconfg.annotation.format.Config(type = ModConfig.Type.CLIENT)
public class Config {
	private static boolean getFalse() {
		return false;
	}
	
	private static final AnnoCFG CFG = new AnnoCFG(FMLJavaModLoadingContext.get().getModEventBus(), Config.class);
	
	@Comment("Generic options")
	@CFGSegment("GeneralOptions")
	public static class GeneralOptions {
		@Name("EnableDynamicLights")
		@Comment({
				"Whether or not tinged lights should use dynamic lighting",
				"Changing this requires a resource reload (F3+T)"
		})
		@Default(valueBoolean = true)
		public static boolean dynamicLights = true;
		
		@Name("DumpShaders")
		@Comment({
				"Whether or not tinged lights should write patched shaders onto the disk",
				"Only really useful for resource pack devs or other mod devs"
		})
		@Default(valueBoolean = false)
		public static boolean dumpShaders = false;
	}
	
	@Comment({
			"Options for block rendering",
			"Changing these requires reload chunk renderers (F3+A)"
	})
	@CFGSegment("TesselationOptions")
	public static class TesselationOptions {
		@Name("AOIntensity")
		@Comment({
				"How defined ambient occlusion should be",
				"I think 0 is how it is in vanilla, hard to tell due to vanilla not having colored lights"
		})
		@Default(valueF = 0.125f)
		@FloatRange(minV = 0, maxV = 1)
		public static float aoIntensity = 0.125f;
		
		@CFGSegment("Vertex Sorting")
		public static class VertexSortingOptions {
			@Name("SortVertices")
			@Comment({
					"Attempts to avoid jagged edges with smooth lighting",
					"Vanilla does not have this"
			})
			@Default(valueBoolean = true)
			public static boolean sortVertices = true;
			
			@CFGSegment("AOSorting")
			public static class SortingOptions {
				@Name("SortOutside")
				@Comment("Whether or not AO sort should sort outer corners")
				@Default(valueBoolean = true)
				public static boolean sortOutside = true;
				
				@Name("SortDual")
				@Comment("Whether or not AO sort should sort perpendicular corners")
				@Default(valueBoolean = true)
				public static boolean sortPerpendicular = true;
				
				@Name("SortInner")
				@Comment("Whether AO sort should sort inner corners")
				@Default(valueBoolean = true)
				public static boolean sortInner = true;
				
				@Name("BoxedOutside")
				@Comment("Boxes off AO when there's an outside corner")
				@Default(valueBoolean = true)
				public static boolean boxOutside = true;
				
				@Name("BoxedDual")
				@Comment("Boxes off AO when there's two perpendicular corners")
				@Default(valueBoolean = true)
				public static boolean boxPerpendicular = true;
				
				@Name("BoxedInner")
				@Comment("Whether AO sort should create boxes for inner corners")
				@Default(valueBoolean = true)
				public static boolean boxedInner = true;
			}
		}
	}
	
	@Skip
	public static boolean wroteLightShader = false;
	
	public static void init() {
//		CFG.create(ModConfig.Type.SERVER, ModLoadingContext.get().getActiveNamespace() + "_server.toml");
	}
}
