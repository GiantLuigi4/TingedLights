package tfc.tingedlights.api;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.TingedLightsItemAttachments;

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
				
				if (stack.getItem() instanceof TingedLightsItemAttachments attachments) {
					Light light = attachments.createLight(stack, player);
					
					if (light != null) {
						int brightness = attachments.getBrightness(stack, player);
						
						int sub = 2;
						for (int i = 1; i < 16; i++) {
							if ((brightness - i - sub) < 0) continue;
							
							Color old = colors[(brightness - i - sub)];
							colors[(brightness - i - sub)] = new Color(
									old.r() + light.getColor((byte) (i)).r(),
									old.g() + light.getColor((byte) (i)).g(),
									old.b() + light.getColor((byte) (i)).b()
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
