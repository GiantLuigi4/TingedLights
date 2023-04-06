package tfc.tingedlights.api;

import net.minecraft.world.level.block.Block;
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
}
