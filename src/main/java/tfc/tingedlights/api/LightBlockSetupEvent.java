package tfc.tingedlights.api;

import net.minecraftforge.eventbus.api.Event;

public class LightBlockSetupEvent extends Event {
	private final LightBlockApi API;
	
	public LightBlockSetupEvent(LightBlockApi API) {
		this.API = API;
	}
	
	public LightBlockApi getAPI() {
		return API;
	}
}
