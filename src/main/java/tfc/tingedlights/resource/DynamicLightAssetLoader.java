package tfc.tingedlights.resource;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;
import tfc.tingedlights.data.access.TingedLightsItemAttachments;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class DynamicLightAssetLoader extends SimpleJsonResourceReloadListener {
	Gson mySpecificGsonObjectBecauseMojangWantedMeToHaveOne;
	
	public DynamicLightAssetLoader(Gson p_10768_) {
		super(p_10768_, "dynamic_lights");
		mySpecificGsonObjectBecauseMojangWantedMeToHaveOne = p_10768_;
	}
	
	BiFunction<ItemStack, Entity, Light> defaultLightProvider = (a, b) -> {
		if (a.getItem() instanceof BlockItem c) {
			if (c.getBlock() instanceof TingedLightsBlockAttachments attachments) {
				return attachments.createLight(c.getBlock().defaultBlockState(), null, null);
			}
		}
		return null;
	};
	BiFunction<ItemStack, Entity, Integer> defaultBrightnessProvider = (a, b) -> {
		if (a.getItem() instanceof BlockItem c) {
			if (c.getBlock() instanceof TingedLightsBlockAttachments attachments) {
				return attachments.getBrightness(c.getBlock().defaultBlockState(), null, null);
			}
		}
		return 0;
	};
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
		HashMap<Item, Pair<BiFunction<ItemStack, Entity, Light>, BiFunction<ItemStack, Entity, Integer>>> providerHashMap = new HashMap<>();
		
		for (Map.Entry<ResourceLocation, JsonElement> resourceLocationJsonElementEntry : pObject.entrySet()) {
			JsonObject object = resourceLocationJsonElementEntry.getValue().getAsJsonObject();
			for (String s : object.keySet()) {
				Light light = parseLightType(
						object.getAsJsonObject(s),
						resourceLocationJsonElementEntry.getKey().toString(),
						s
				);
				int brightness = getIntOrDefault(object.getAsJsonObject(s), "brightness", 15, resourceLocationJsonElementEntry.getKey().toString(), s + "/brightness");
				providerHashMap.put(
						Registry.ITEM.get(new ResourceLocation(s)),
						Pair.of(
								(i, e) -> light,
								(i, e) -> brightness
						)
				);
			}
		}
		
		for (Item block : Registry.ITEM) {
			if (block instanceof TingedLightsItemAttachments attachments) {
				if (providerHashMap.containsKey(block)) {
					Pair<BiFunction<ItemStack, Entity, Light>, BiFunction<ItemStack, Entity, Integer>> provider = providerHashMap.get(block);
					attachments.setFunctions(provider.getFirst(), provider.getSecond());
				} else
					attachments.setFunctions(defaultLightProvider, defaultBrightnessProvider);
			}
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
