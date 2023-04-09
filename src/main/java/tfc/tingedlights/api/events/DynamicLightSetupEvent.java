package tfc.tingedlights.api.events;

import net.minecraftforge.eventbus.api.Event;
import tfc.tingedlights.api.DynamicLightApi;

public class DynamicLightSetupEvent extends Event {
	private final DynamicLightApi API;
	
	public DynamicLightSetupEvent(DynamicLightApi API) {
		this.API = API;
	}
	
	public DynamicLightApi getAPI() {
		return API;
	}
}
