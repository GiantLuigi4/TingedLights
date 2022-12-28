package tfc.tingedlights.mixin.backend;

import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.ILightEngine;

@Mixin(LevelLightEngine.class)
public class LightEngineMixin implements ILightEngine {
	LightManager manager;
	
	@Override
	public LightManager getManager() {
		return manager;
	}
	
	@Override
	public void setManager(LightManager manager) {
		this.manager = manager;
	}
}
