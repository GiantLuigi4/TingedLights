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
import tfc.tingedlights.utils.config.annoconfg.annotation.value.IntRange;

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
				"If vanilla AO is enabled, then 0 is vanilla",
				"If vanilla AO not enabled, then",
				"- with SmoothingMode 0, 0.25 gets it close to vanilla",
				"- with SmoothingMode 1 and 2, 0.4 gets it close to vanilla"
		})
		@Default(valueF = 0.4f)
		@FloatRange(minV = 0, maxV = 1)
		public static float aoIntensity = 0.4f;
		
		@Name("RemoveVanillaAO")
		@Comment({
				"Whether or not to remove vanilla AO",
				"Turning this on will most likely make smooth lighting look nicer"
		})
		@Default(valueBoolean = true)
		public static boolean removeVanillaAO;
		
		@Name("SoftAO")
		@Comment({
				"Whether or not AO should apply around light sources that are also full blocks"
		})
		@Default(valueBoolean = true)
		public static boolean allowSoftAO;
		
		@Name("SmoothingMode")
		@Comment({
				"How smooth lighting should be calculated",
				"Each of these has a different appearance",
				"I believe 1 is most like how vanilla behaves",
				"0=sum, 1=alt_sum, 2=max"
		})
		@Default(valueI = 2)
		@IntRange(minV = 0, maxV = 2)
		public static int aoMode;
		
		@Name("FastDraw")
		@Comment({
				"Whether or not to use a faster rendering method",
				"This may or may not cause incompatibilities with other mods that change block rendering"
		})
		@Default(valueBoolean = true)
		public static boolean fastDraw;
		
		@Comment("For vanilla-style AO, turn everything under this category to false")
		@CFGSegment("VertexSorting")
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
				@Name("OutsideStyle")
				@Comment({
						"Outside corner AO style",
						"Sodium uses 2",
						"0=unsorted, 1=boxed, 2=sloped, 3=vanilla"
				})
				@Default(valueI = 3)
				@IntRange(minV = 0, maxV = 3)
				public static int boxOutside = 3;
				
				@Name("DualStyle")
				@Comment({
						"Perpendicular AO style",
						"0=unsorted, 1=boxed, 2=sloped"
				})
				@Default(valueI = 1)
				public static int boxPerpendicular = 1;
				
				@Name("InnerStyle")
				@Comment({
						"Inner corner AO style",
						"0=unsorted, 1=boxed, 2=sloped"
				})
				@Default(valueI = 1)
				public static int boxedInner = 1;
			}
		}
	}
	
	@Skip
	public static boolean wroteLightShader = false;
	
	public static void init() {
//		CFG.create(ModConfig.Type.SERVER, ModLoadingContext.get().getActiveNamespace() + "_server.toml");
	}
}
