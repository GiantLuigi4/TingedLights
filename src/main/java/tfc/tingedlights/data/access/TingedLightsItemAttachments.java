package tfc.tingedlights.data.access;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import tfc.tingedlights.api.data.Light;

import java.util.function.BiFunction;

public interface TingedLightsItemAttachments {
	Light createLight(ItemStack pState, Entity entity);
	
	int getBrightness(ItemStack pState, Entity entity);
	
	void setFunctions(
			BiFunction<ItemStack, Entity, Light> lightCreation,
			BiFunction<ItemStack, Entity, Integer> brightnessGetter
	);
}
