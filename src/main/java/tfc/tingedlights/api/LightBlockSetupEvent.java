package tfc.tingedlights.api;

import net.minecraftforge.eventbus.api.Event;

public class LightBlockSetupEvent extends Event {
	private static final LightBlockApi API = new LightBlockApi();
	
	public static LightBlockApi getAPI() {
		return API;
	}
}
