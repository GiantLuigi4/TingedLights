package tfc.tingedlights.mixin.backend;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.ILightEngine;
import tfc.tingedlights.data.access.LevelWithColoredLightSupport;
import tfc.tingedlights.data.struct.LightNode;

import java.util.Collection;
import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements LevelWithColoredLightSupport {
	@Unique
	LightManager manager = new LightManager((Level) (Object) this);
	
	// so there's not really any way to know when a client world object is leaving active usage
	// and thus I use this
	// couldn't really think of any other way reliable to do it
	@SuppressWarnings("unused")
	@Unique
	Object gcHack = new Object() {
		@Override
		protected void finalize() {
			manager.close();
		}
	};
	
	@Override
	public LightManager getManager() {
		return manager;
	}
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(ClientPacketListener p_205505_, ClientLevel.ClientLevelData p_205506_, ResourceKey p_205507_, Holder p_205508_, int p_205509_, int p_205510_, Supplier p_205511_, LevelRenderer p_205512_, boolean p_205513_, long p_205514_, CallbackInfo ci) {
		ILightEngine engine = (ILightEngine) ((Level) (Object) this).getLightEngine();
		engine.setManager(manager);
	}
	
	@Override
	public void addLight(Light light) {
		manager.addLight(light);
	}
	
	@Override
	public void removeLight(Light light) {
		manager.removeLight(light);
	}
	
	@Override
	public void removeLights(BlockPos pPos) {
		manager.removeLights(pPos);
	}
	
	@Override
	public void updateNeighbors(BlockPos pPos) {
		manager.updateNeighbors(pPos);
	}
	
	@Override
	public Collection<LightNode> getSources(BlockPos pos) {
		return manager.getSources(pos);
	}
	
	@Inject(at = @At("TAIL"), method = "onChunkLoaded")
	public void postLoadChunk(ChunkPos pChunkPos, CallbackInfo ci) {
		manager.loadChunk(((Level) (Object) this).getChunk(pChunkPos.x, pChunkPos.z));
	}
	
	@Inject(at = @At("TAIL"), method = "unload")
	public void postLoadChunk(LevelChunk pChunk, CallbackInfo ci) {
		manager.unloadChunk(pChunk.getPos());
	}
}
