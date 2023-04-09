package tfc.tingedlights.resource;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tfc.tingedlights.api.LightBlockApi;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.api.data.LightProvider;
import tfc.tingedlights.api.interfaces.QuadFunction;
import tfc.tingedlights.api.interfaces.TriFunction;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

import java.util.HashMap;
import java.util.Map;

public class TingedLightAssetLoader extends SimpleJsonResourceReloadListener {
	Gson mySpecificGsonObjectBecauseMojangWantedMeToHaveOne;
	
	public TingedLightAssetLoader(Gson p_10768_) {
		super(p_10768_, "light_colors");
		mySpecificGsonObjectBecauseMojangWantedMeToHaveOne = p_10768_;
	}
	
	private static TriFunction<BlockState, BlockGetter, BlockPos, Light> defaultLightProvider = (pState, pLevel, pPos) -> null;
	private static TriFunction<BlockState, BlockGetter, BlockPos, Integer> defaultBrightnessProvider = (pState, pLevel, pPos) -> 0;
	private static TriFunction<BlockState, BlockGetter, BlockPos, Boolean> defaultIsSource = (pState, pLevel, pPos) -> false;
	private static QuadFunction<BlockState, BlockState, BlockGetter, BlockPos, Boolean> defaultUpdateChecker = (pState, pOld, pLevel, pPos) -> {
		if (pOld instanceof TingedLightsBlockAttachments yep)
			yep.providesLight(pOld, pLevel, pPos);
		return !pState.getBlock().equals(pOld.getBlock());
	};
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
		HashMap<Block, PerStateLightProvider> providerHashMap = new HashMap<>();
		LightProvider theDefaults = null;
		
		for (ResourceLocation location : pObject.keySet()) {
			JsonElement element = pObject.get(location);
			if (element instanceof JsonObject object) {
				Map<String, Light> cache = parseTypes(object, location.toString());
				if (cache == null) continue;
				
				object = object.getAsJsonObject("lights");
				for (String s : object.keySet()) {
					if (s.equals("tingedlights:default")) {
						theDefaults = parseDefaults(cache, object.getAsJsonObject(s), location.toString(), s);
					} else {
						Block block = Registry.BLOCK.get(new ResourceLocation(s));
						if (block != null) { // TODO: support tags
							if (Registry.BLOCK.getKey(block).equals(new ResourceLocation(s))) {
								JsonElement element1 = object.get(s);
								if (element1 instanceof JsonObject obj) {
									Pair<StateIdentifier, LightProvider> provider = parseProvider(cache, obj, location.toString(), s);
									PerStateLightProvider lightProvider = providerHashMap.get(block);
									if (lightProvider == null)
										providerHashMap.put(block, lightProvider = new PerStateLightProvider());
									lightProvider.addProvider(provider.getFirst(), provider.getSecond());
								} else if (element1 instanceof JsonArray array) {
									PerStateLightProvider lightProvider = providerHashMap.get(block);
									if (lightProvider == null)
										providerHashMap.put(block, lightProvider = new PerStateLightProvider());
									for (JsonElement jsonElement : array) {
										if (jsonElement instanceof JsonObject obj) {
											Pair<StateIdentifier, LightProvider> provider = parseProvider(cache, obj, location.toString(), s);
											lightProvider.addProvider(provider.getFirst(), provider.getSecond());
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		for (PerStateLightProvider value : providerHashMap.values())
			value.sort();
		
		TriFunction<BlockState, BlockGetter, BlockPos, Light> defaultLightProvider = TingedLightAssetLoader.defaultLightProvider;
		TriFunction<BlockState, BlockGetter, BlockPos, Integer> defaultBrightnessProvider = TingedLightAssetLoader.defaultBrightnessProvider;
		TriFunction<BlockState, BlockGetter, BlockPos, Boolean> defaultIsSource = TingedLightAssetLoader.defaultIsSource;
		QuadFunction<BlockState, BlockState, BlockGetter, BlockPos, Boolean> defaultUpdateChecker = TingedLightAssetLoader.defaultUpdateChecker;
		if (theDefaults != null) {
			defaultLightProvider = theDefaults::createLight;
			defaultBrightnessProvider = theDefaults::getBrightness;
			defaultIsSource = theDefaults::providesLight;
			defaultUpdateChecker = theDefaults::needsUpdate;
		}
		
		// TODO: only update blocks that actually were light sources before resource reload
		for (Block block : Registry.BLOCK) {
			if (block instanceof TingedLightsBlockAttachments attachments) {
				if (providerHashMap.containsKey(block)) {
					// TODO: optimize mem usage (deduplicate functions)
					LightProvider provider = providerHashMap.get(block);
					if (provider instanceof PerStateLightProvider perState)
						provider = perState.maybeBake();
					attachments.setFunctions(provider::createLight, provider::getBrightness, provider::providesLight, provider::needsUpdate);
				} else
					attachments.setFunctions(defaultLightProvider, defaultBrightnessProvider, defaultIsSource, defaultUpdateChecker);
			}
		}
		
		LightBlockApi.postSetup();
	}
	
	public Map<String, Light> parseTypes(JsonObject object, String file) {
		try {
			object = object.getAsJsonObject("types");
			HashMap<String, Light> cache = new HashMap<>();
			for (String s : object.keySet())
				cache.put(s, parseLightType(object.getAsJsonObject(s), file, "types/" + s));
			return cache;
		} catch (Throwable err) {
			System.out.println("Failed to parse light types in " + file);
			err.printStackTrace();
			return null;
		}
	}
	
	public Light parseLightType(JsonObject asJsonObject, String file, String element) {
		Color startColor = null;
		Color endColor = null;
		if (asJsonObject.has("start")) {
			JsonArray array = asJsonObject.getAsJsonArray("start");
			float divisor = 255f;
			if (array.size() > 3) {
				divisor *= array.get(3).getAsFloat();
			}
			startColor = new Color(
					array.get(0).getAsFloat() / divisor,
					array.get(1).getAsFloat() / divisor,
					array.get(2).getAsFloat() / divisor
			);
			array = asJsonObject.getAsJsonArray("end");
			divisor = 255f;
			if (array.size() > 3) {
				divisor *= array.get(3).getAsFloat();
			}
			endColor = new Color(
					array.get(0).getAsFloat() / divisor,
					array.get(1).getAsFloat() / divisor,
					array.get(2).getAsFloat() / divisor
			);
		} else {
			JsonArray array = asJsonObject.getAsJsonArray("color");
			float divisor = 255f;
			if (array.size() > 3) {
				divisor *= array.get(3).getAsFloat();
			}
			startColor = endColor = new Color(
					array.get(0).getAsFloat() / divisor,
					array.get(1).getAsFloat() / divisor,
					array.get(2).getAsFloat() / divisor
			);
		}
		
		int transition = getIntOrDefault(asJsonObject, "transition_start", 15, file, element);
		boolean distanceFade = getBoolOrDefault(asJsonObject, "distance_fade", true, file, element);
		
		return new Light(startColor, endColor, transition, distanceFade);
	}
	
	private LightProvider parseDefaults(Map<String, Light> lights, JsonObject asJsonObject, String file, String element) {
		// I may or may not end up having features exclusive to the defaults
		return parseProvider(lights, asJsonObject, file, element).getSecond();
	}
	
	private Pair<StateIdentifier, LightProvider> parseProvider(Map<String, Light> lights, JsonObject asJsonObject, String file, String element) {
		String type = asJsonObject.getAsJsonPrimitive("type").getAsString();
		Light light = lights.get(type);
		
		int brightness = getIntOrDefault(asJsonObject, "brightness", 15, file, element);
		
		Block block = Registry.BLOCK.get(new ResourceLocation(element));
		Map<String, String> state = new HashMap<>();
		StateIdentifier identifier = new StateIdentifier(block, state);
		if (asJsonObject.has("state")) {
			JsonObject stateObj = asJsonObject.getAsJsonObject("state");
			for (String s : stateObj.keySet()) {
				state.put(s, stateObj.get(s).getAsString());
			}
		}
		
		if (brightness == -1) {
			return Pair.of(identifier, new LightProvider() {
				@Override
				public Light createLight(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
					int emit = pState.getLightEmission(pLevel, pPos);
					if (emit == 0) return null;
					return light;
				}
				
				@Override
				public int getBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
					return pState.getLightEmission(pLevel, pPos);
				}
				
				@Override
				public boolean providesLight(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
					int emit = pState.getLightEmission(pLevel, pPos);
					return emit != 0;
				}
			});
		} else {
			return Pair.of(identifier, new LightProvider() {
				@Override
				public Light createLight(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
					return light;
				}
				
				@Override
				public int getBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
					return brightness;
				}
				
				@Override
				public boolean providesLight(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
					return true;
				}
			});
		}
	}
	
	protected int getIntOrDefault(JsonObject object, String name, int defaultValue, String file, String element) {
		if (object.has(name)) {
			JsonElement element1 = object.get(name);
			if (element1 instanceof JsonPrimitive primitive) {
				try {
					return primitive.getAsInt();
				} catch (Throwable ignored) {
				}
			}
			throw new RuntimeException("Could not parse light json " + file + " because " + element + " has an incorrect data type for " + name + "; should be an integer.");
		}
		return defaultValue;
	}
	
	protected boolean getBoolOrDefault(JsonObject object, String name, boolean defaultValue, String file, String element) {
		if (object.has(name)) {
			JsonElement element1 = object.get(name);
			if (element1 instanceof JsonPrimitive primitive) {
				try {
					return primitive.getAsBoolean();
				} catch (Throwable ignored) {
				}
			}
			throw new RuntimeException("Could not parse light json " + file + " because " + element + " has an incorrect data type for " + name + "; should be an boolean.");
		}
		return defaultValue;
	}
}
