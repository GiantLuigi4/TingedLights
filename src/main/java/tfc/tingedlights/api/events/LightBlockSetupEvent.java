package tfc.tingedlights.api.events;

import net.minecraftforge.eventbus.api.Event;
import tfc.tingedlights.api.LightBlockApi;

public class LightBlockSetupEvent extends Event {
	private final LightBlockApi API;
	
	public LightBlockSetupEvent(LightBlockApi API) {
		this.API = API;
	}
	
	public LightBlockApi getAPI() {
		return API;
	}
}
