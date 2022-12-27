package tfc.tingedlights;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.LevelWithColoredLightSupport;
import tfc.tingedlights.resource.TingedLightAssetLoader;

@Mod("tinged_lights")
public class TingedLights {
	private static final Logger LOGGER = LogManager.getLogger();
	
	public TingedLights() {
		MinecraftForge.EVENT_BUS.addListener(TingedLights::onChat);
		
		if (FMLEnvironment.dist.isClient()) {
			ReloadableResourceManager reloadableResourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
			reloadableResourceManager.registerReloadListener(new TingedLightAssetLoader(new GsonBuilder().setLenient().create()));
		}
	}
	
	public static void onChat(ClientChatEvent event) {
		try {
			if (event.getMessage().startsWith("addlight")) {
				String[] split = event.getMessage().split(" ");
				BlockPos pos = new BlockPos(
						Integer.parseInt(split[1]),
						Integer.parseInt(split[2]),
						Integer.parseInt(split[3])
				);
				String colors = split[4];
				String[] colorsSplit = new String[]{colors, colors};
				if (colors.contains("->")) {
					colorsSplit = colors.split("->");
				}
				Color[] colorsOut = new Color[2];
				for (int i = 0; i < colorsSplit.length; i++) {
					String s = colorsSplit[i];
					String[] strs = s.split(";");
					colorsOut[i] = new Color(
							Integer.parseInt(strs[0]) / 255f,
							Integer.parseInt(strs[1]) / 255f,
							Integer.parseInt(strs[2]) / 255f
					);
				}
				
				LevelWithColoredLightSupport lightLevel = (LevelWithColoredLightSupport) Minecraft.getInstance().level;
				
				byte v = (byte) 15;
				try {
					v = Byte.parseByte(split[5]);
				} catch (Throwable ignored) {
				}
				byte threshold = (byte) 7;
				try {
					threshold = Byte.parseByte(split[6]);
				} catch (Throwable ignored) {
				}
				
				boolean dFade = true;
				try {
					if (split[7].equals("0")) {
						dFade = false;
					}
				} catch (Throwable ignored) {
				}
				
				lightLevel.addLight(new Light(colorsOut[0], colorsOut[1], threshold, v, pos, dFade));
			}
		} catch (Throwable ignored) {
		}
	}
}
