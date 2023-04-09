package tfc.tingedlights.api;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import tfc.tingedlights.api.data.LightProvider;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

public class LightBlockApi {
	public void setLightBlock(Block block, LightProvider provider) {
		if (block instanceof TingedLightsBlockAttachments attachments) {
			attachments.setFunctions(
					provider::createLight,
					provider::getBrightness,
					provider::providesLight,
					provider::needsUpdate
			);
		}
	}
	
	public static void postSetup() {
		LightBlockApi api = new LightBlockApi();
		LightBlockSetupEvent event = new LightBlockSetupEvent(api);
		MinecraftForge.EVENT_BUS.post(event);
	}
}
