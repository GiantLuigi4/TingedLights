package tfc.tingedlights.mixin.backend;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;
import tfc.tingedlights.data.access.TingedLightsItemAttachments;

import java.util.function.BiFunction;

@Mixin(Item.class)
public class ItemMixin implements TingedLightsItemAttachments {
	@Unique
	BiFunction<ItemStack, Entity, Light> lightCreation = (a, b) -> {
		if (a.getItem() instanceof BlockItem c) {
			if (c.getBlock() instanceof TingedLightsBlockAttachments attachments) {
				return attachments.createLight(c.getBlock().defaultBlockState(), null, null);
			}
		}
		return null;
	};
	@Unique
	BiFunction<ItemStack, Entity, Integer> brightnessGetter = (a, b) -> {
		if (a.getItem() instanceof BlockItem c) {
			if (c.getBlock() instanceof TingedLightsBlockAttachments attachments) {
				return attachments.getBrightness(c.getBlock().defaultBlockState(), null, null);
			}
		}
		return 0;
	};
	
	@Override
	public Light createLight(ItemStack pState, Entity entity) {
		return lightCreation.apply(pState, entity);
	}
	
	@Override
	public int getBrightness(ItemStack pState, Entity entity) {
		return brightnessGetter.apply(pState, entity);
	}
	
	@Override
	public void setFunctions(
			BiFunction<ItemStack, Entity, Light> lightCreation,
			BiFunction<ItemStack, Entity, Integer> brightnessGetter
	) {
		this.lightCreation = lightCreation == null ? this.lightCreation : lightCreation;
		this.brightnessGetter = brightnessGetter == null ? this.brightnessGetter : brightnessGetter;
	}
}
