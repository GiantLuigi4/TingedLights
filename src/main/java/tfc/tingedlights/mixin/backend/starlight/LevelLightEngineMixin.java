package tfc.tingedlights.mixin.backend.starlight;

import ca.spottedleaf.starlight.common.light.StarLightInterface;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.LightBlender;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.ColoredLightEngine;
import tfc.tingedlights.data.access.IHoldColoredLights;
import tfc.tingedlights.util.starlight.ColoredLightInterface;
import tfc.tingedlights.utils.LightInfo;

import java.util.*;

@Mixin(LevelLightEngine.class)
public class LevelLightEngineMixin implements ColoredLightEngine {
	@Shadow
	@Final
	protected LevelHeightAccessor levelHeightAccessor;
	@Unique
	final Map<Light, StarLightInterface> engines = new HashMap<>();
	@Unique
	Map.Entry<Light, StarLightInterface>[] enginesArray = new Map.Entry[0];
	@Unique
	final HashSet<ChunkPos> enabledLights = new HashSet<>();
	@Unique
	final HashSet<SectionPos> enabledSections = new HashSet<>();
	@Unique
	int totalEngines = 0;
	
	@Unique
	boolean enable = false;
	@Unique
	LightChunkGetter lightChunkGetter;
	@Unique
	BlockGetter level;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(LightChunkGetter p_75805_, boolean p_75806_, boolean p_75807_, CallbackInfo ci) {
		if (p_75805_ instanceof ClientLevel clvl) {
			lightChunkGetter = clvl.getChunkSource();
			level = clvl;
			enable = true;
		} else if (p_75805_ instanceof ClientChunkCache cChunkCache) {
			lightChunkGetter = cChunkCache;
			level = cChunkCache.getLevel();
			enable = true;
		} else {
			lightChunkGetter = null;
			enable = false;
		}
	}
	
	protected void addChunk(Light key, ChunkPos pos, StarLightInterface engine) {
		BlockGetter chunk = lightChunkGetter.getChunkForLighting(pos.x, pos.z);
		if (chunk instanceof IHoldColoredLights colorLightHolder) {
			for (Collection<LightInfo> source : colorLightHolder.getSources()) {
				for (LightInfo lightInfo : source) {
					if (lightInfo.light().equals(key)) {
						engine.blockChange(lightInfo.pos());
					}
				}
			}
		}
	}
	
	protected StarLightInterface getEngine(Light key) {
		return engines.computeIfAbsent(key, (k) -> {
			totalEngines++;
			
			ColoredLightInterface engine = new ColoredLightInterface((LevelLightEngine) (Object) this, (Level) level, key, lightChunkGetter);
			StarLightInterface casted = (StarLightInterface) (Object) engine;
			for (SectionPos enabledSection : enabledSections)
				casted.sectionChange(enabledSection, true);
			
			for (ChunkPos enabledLight : enabledLights) addChunk(key, enabledLight, casted);
			
			return casted;
		});
	}
	
	@Override
	public void updateLight(Light light, BlockPos pos) {
		if (enable) {
			getEngine(light).blockChange(pos);
		}
	}
	
	@Override
	public Color getColor(BlockPos pos) {
		return getColor(pos, false);
	}
	
	@Override
	public Color getColor(BlockPos pos, boolean allowNull) {
		for (int i1 = 0; i1 < 10; i1++) { // this shouldn't really be able to fail more than once in a row
			try {
				//noinspection RedundantSuppression
				Map.Entry<Light, StarLightInterface>[] collection = enginesArray;
				int engineCount = collection.length;
				
				Color[] colors = new Color[engineCount];
				boolean hasNonZero = false;
				
				for (int i = 0; i < engineCount; i++) {
					Map.Entry<Light, StarLightInterface> lightBlockLightEngineEntry = collection[i];
					int val = lightBlockLightEngineEntry.getValue().getBlockReader().getLightValue(pos);
					if (val == 0) continue;
					colors[i] = lightBlockLightEngineEntry.getKey().getColor((byte) val);
					hasNonZero = true;
				}
				
				if (allowNull) {
					if (!hasNonZero)
						return null;
				} else if (!hasNonZero) return Color.BLACK;
				
				return LightBlender.blend(colors);
			} catch (Throwable err) {
				if (err instanceof ConcurrentModificationException) continue;
				if (!FMLEnvironment.production) err.printStackTrace();
			}
		}
		return Color.BLACK;
	}
	
	@Override
	public void removeLights(BlockPos pPos) {
		for (Light light : engines.keySet())
			updateLight(light, pPos);
	}
	
	@Override
	public void updateNeighbors(BlockPos pPos) {
		for (Light light : engines.keySet())
			updateLight(light, pPos);
	}
	
	@Override
	public void enableEngine(Light light) {
		getEngine(light);
	}
	
	@Unique
	public void preUpdateSection(SectionPos pPos, boolean pIsEmpty) {
		if (enable) {
			if (pIsEmpty) enabledSections.remove(pPos);
			else enabledSections.add(pPos);
			
			for (StarLightInterface value : engines.values())
				value.sectionChange(pPos, pIsEmpty);
		}
	}
	
	@Unique
	public void postEnableLights(ChunkPos p_75812_, boolean p_75813_) {
		if (enable) {
			if (p_75813_) enabledLights.add(p_75812_);
			else enabledLights.remove(p_75812_);
		}
	}
	
	@Unique
	public void preRunUpdates() {
		if (enable) {
			if (enginesArray.length != totalEngines)
				enginesArray = engines.entrySet().toArray(new Map.Entry[0]);
			
			for (int i1 = 0; i1 < 10; i1++) { // this shouldn't really be able to fail more than once in a row
				try {
					StarLightInterface[] collection = engines.values().toArray(new StarLightInterface[0]);
					
					for (StarLightInterface value : collection)
						value.propagateChanges();
					
					return;
				} catch (Throwable ignored) {
				}
			}
		}
	}
}
