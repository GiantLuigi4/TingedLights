package tfc.tingedlights;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.tingedlights.resource.DynamicLightAssetLoader;
import tfc.tingedlights.resource.TingedLightAssetLoader;
import tfc.tingedlights.utils.config.Config;

@Mod("tinged_lights")
public class TingedLights {
	public TingedLights() {
		if (FMLEnvironment.dist.isClient()) {
			Config.init();
			
			ReloadableResourceManager reloadableResourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
			Gson gson = new GsonBuilder().setLenient().create();
			reloadableResourceManager.registerReloadListener(new TingedLightAssetLoader(gson));
			reloadableResourceManager.registerReloadListener(new DynamicLightAssetLoader(gson));
		}
	}
}
