package tfc.tingedlights.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

import java.util.Arrays;

public class DynamicLightApi {
	private static final Color[] colors = new Color[15];
	
	static {
		Arrays.fill(colors, Color.BLACK);
	}
	
	public static void tick() {
		if (Minecraft.getInstance().player != null) {
			Player player = Minecraft.getInstance().player;
			Arrays.fill(colors, Color.BLACK);
			
			for (InteractionHand value : InteractionHand.values()) {
				ItemStack stack = player.getItemInHand(value);
				
				if (stack.getItem() instanceof BlockItem) {
					Block block = ((BlockItem) stack.getItem()).getBlock();
					TingedLightsBlockAttachments attachments = ((TingedLightsBlockAttachments) block);
					if (attachments.providesLight(block.defaultBlockState(), null, null)) {
						Light light = attachments.createLight(block.defaultBlockState(), null, null);
						int brightness = attachments.getBrightness(block.defaultBlockState(), null, null);
						
						for (int i = 1; i < 15; i++) {
							if (brightness - i < 0) continue;
							
							Color old = colors[brightness - i];
							colors[brightness - i] = new Color(
									old.r() + light.getColor((byte) i).r(),
									old.g() + light.getColor((byte) i).g(),
									old.b() + light.getColor((byte) i).b()
							);
						}
					}
				}
			}
		}
	}
	
	public static Color getColor(int index) {
		return colors[index];
	}
}
