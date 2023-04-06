package tfc.tingedlights.mixin.backend;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.LevelWithColoredLightSupport;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin implements LevelWithColoredLightSupport {
	@Shadow
	public abstract ClientChunkCache getChunkSource();
	
	@Override
	public LightManager getManager() {
		return (LightManager) getChunkSource().getLightEngine();
	}
	
	@Override
	public void updateLight(Light light, BlockPos pos) {
		getManager().updateLight(light, pos);
	}
	
	@Override
	public Color getColor(BlockPos pos) {
		return getManager().getColor(pos);
	}
	
	@Override
	public Color getColor(BlockPos pos, boolean bl) {
		return getManager().getColor(pos, bl);
	}
	
	@Override
	public void removeLights(BlockPos pPos) {
		getManager().removeLights(pPos);
	}
	
	@Override
	public void updateNeighbors(BlockPos pPos) {
		getManager().updateNeighbors(pPos);
	}
}
