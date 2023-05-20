package tfc.tingedlights.mixin.backend.vanilla;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.tingedlights.LightBlender;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.ColoredLightEngine;
import tfc.tingedlights.data.access.IHoldColoredLights;
import tfc.tingedlights.util.vanilla.ColoredBlockLightingEngine;
import tfc.tingedlights.utils.LightInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Mixin(LevelLightEngine.class)
public class LevelLightEngineMixin implements ColoredLightEngine {
	@Shadow
	@Final
	protected LevelHeightAccessor levelHeightAccessor;
	@Unique
	final Map<Light, BlockLightEngine> engines = new HashMap<>();
	@Unique
	Map.Entry<Light, BlockLightEngine>[] enginesArray = new Map.Entry[0];
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
	
	@Inject(at = @At("HEAD"), method = "updateSectionStatus")
	public void preUpdateSection(SectionPos pPos, boolean pIsEmpty, CallbackInfo ci) {
		if (enable) {
			if (pIsEmpty) enabledSections.remove(pPos);
			else enabledSections.add(pPos);
			
			for (BlockLightEngine value : engines.values()) value.updateSectionStatus(pPos, pIsEmpty);
		}
	}
	
	@Inject(at = @At("HEAD"), method = "enableLightSources")
	public void postEnableLights(ChunkPos p_75812_, boolean p_75813_, CallbackInfo ci) {
		if (enable) {
			if (p_75813_) enabledLights.add(p_75812_);
			else enabledLights.remove(p_75812_);
			
			for (Map.Entry<Light, BlockLightEngine> lightBlockLightEngineEntry : engines.entrySet())
				lightBlockLightEngineEntry.getValue().enableLightSources(p_75812_, p_75813_);
		}
	}
	
	@Inject(at = @At("HEAD"), method = "runUpdates")
	public void preRunUpdates(int p_75809_, boolean p_75810_, boolean p_75811_, CallbackInfoReturnable<Integer> cir) {
		if (enable) {
			if (enginesArray.length != totalEngines)
				enginesArray = engines.entrySet().toArray(new Map.Entry[0]);
			
			int v = p_75809_;
			for (int i1 = 0; i1 < 10; i1++) { // this shouldn't really be able to fail more than once in a row
				try {
					BlockLightEngine[] collection = engines.values().toArray(new BlockLightEngine[0]);
					
					for (BlockLightEngine value : collection) {
						if (v == 0) return;
						v -= v - value.runUpdates(v, false, true);
					}
					
					return;
				} catch (Throwable ignored) {
				}
			}
		}
	}
	
	protected void addChunk(Light key, ChunkPos pos, BlockLightEngine engine) {
		BlockGetter chunk = lightChunkGetter.getChunkForLighting(pos.x, pos.z);
		if (chunk instanceof IHoldColoredLights colorLightHolder) {
			for (Collection<LightInfo> source : colorLightHolder.getSources()) {
				for (LightInfo lightInfo : source) {
					if (lightInfo.light().equals(key)) {
						engine.checkBlock(lightInfo.pos());
					}
				}
			}
		}
	}
	
	protected BlockLightEngine getEngine(Light key) {
		return engines.computeIfAbsent(key, (k) -> {
			totalEngines++;
			BlockLightEngine engine = new ColoredBlockLightingEngine(key, lightChunkGetter, level);
			for (SectionPos enabledSection : enabledSections) engine.updateSectionStatus(enabledSection, false);
			for (ChunkPos enabledLight : enabledLights) engine.enableLightSources(enabledLight, true);
			
			for (ChunkPos enabledLight : enabledLights) addChunk(key, enabledLight, engine);
			
			return engine;
		});
	}
	
	@Override
	public void updateLight(Light light, BlockPos pos) {
		if (enable) {
			getEngine(light).checkBlock(pos);
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
				Map.Entry<Light, BlockLightEngine>[] collection = enginesArray;
				int engineCount = collection.length;
				
				Color[] colors = new Color[engineCount];
				boolean hasNonZero = false;
				
				for (int i = 0; i < engineCount; i++) {
					Map.Entry<Light, BlockLightEngine> lightBlockLightEngineEntry = collection[i];
					int val = lightBlockLightEngineEntry.getValue().getLightValue(pos);
					if (val == 0) continue;
					colors[i] = lightBlockLightEngineEntry.getKey().getColor((byte) val);
					hasNonZero = true;
				}
				
				if (allowNull) {
					if (!hasNonZero)
						return null;
				} else if (!hasNonZero) return Color.BLACK;
				
				return LightBlender.blend(colors);
			} catch (Throwable ignored) {
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
}
