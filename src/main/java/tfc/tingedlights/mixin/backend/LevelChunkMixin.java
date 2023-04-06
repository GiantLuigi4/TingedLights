package tfc.tingedlights.mixin.backend;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.LightManager;
import tfc.tingedlights.data.access.IHoldColoredLights;
import tfc.tingedlights.data.access.ILightEngine;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;
import tfc.tingedlights.util.ChunkLoadState;
import tfc.tingedlights.util.OnThread;
import tfc.tingedlights.util.Threading;
import tfc.tingedlights.utils.LightInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements IHoldColoredLights {
	@Unique
	Collection<LightInfo>[] sources;
	
	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V")
	public void postInit(Level pLevel, ChunkPos pPos, UpgradeData pData, LevelChunkTicks pBlockTicks, LevelChunkTicks pFluidTIcks, long pInhabitedTime, LevelChunkSection[] pSections, LevelChunk.PostLoadProcessor pPostLoad, BlendingData p_196862_, CallbackInfo ci) {
		sources = new Collection[((LevelChunk) (Object) this).getSections().length];
		for (int i = 0; i < sources.length; i++)
			sources[i] = new HashSet<>();
		if (Minecraft.getInstance().level != null) {
			// TODO: figure out how to make firstBatch become false only after a set radius of chunks have been recieved, or smth
			wasLoaded = !ChunkLoadState.firstBatch;
		}
	}
	
	@Override
	public Collection<LightInfo>[] getSources() {
		return sources;
	}
	
	@Shadow
	public abstract Stream<BlockPos> getLights();
	
	@Shadow
	public abstract BlockState getBlockState(BlockPos pPos);
	
	@Shadow
	@Final
	private Level level;
	
	@Shadow
	public abstract Level getLevel();
	
	@Unique
	boolean wasLoaded = false;
	
	@Unique
	private static void fill(LevelChunk chunk, Collection<LightInfo>[] sources) {
		LevelChunk lvlChunk = (LevelChunk) (Object) chunk;
		Collection<LightInfo>[] newSources = new Collection[sources.length];
		
		LightManager manager = ((ILightEngine) lvlChunk.getLevel().getLightEngine()).getManager();
		
		for (LevelChunkSection section : lvlChunk.getSections()) {
			int sectionY = (int) SectionPos.blockToSection(section.bottomBlockY());
			sectionY = lvlChunk.getSectionIndexFromSectionY(sectionY);
			
			newSources[sectionY] = new HashSet<>();
			if (section.hasOnlyAir()) continue;
			
			BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
			
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						BlockState state = section.getBlockState(x, y, z);
						blockPos.set(
								x + lvlChunk.getPos().getMinBlockX(),
								y + section.bottomBlockY(), // TODO: check?
								z + lvlChunk.getPos().getMinBlockZ()
						);
						
						if (state.getBlock() instanceof TingedLightsBlockAttachments attachments) {
							if (attachments.providesLight(state, lvlChunk.getLevel(), blockPos)) {
								// makes sure the mutable pos is still in the right spot
								blockPos.set(
										x + lvlChunk.getPos().getMinBlockX(),
										y + section.bottomBlockY(), // TODO: check?
										z + lvlChunk.getPos().getMinBlockZ()
								);
								
								BlockPos immut = blockPos.immutable();
								Light light = attachments.createLight(state, lvlChunk.getLevel(), immut);
								if (light != null) {
									newSources[sectionY].add(new LightInfo(light, immut, (byte) attachments.getBrightness(state, lvlChunk.getLevel(), immut)));
								}
							}
						}
					}
				}
			}
			
			sources[sectionY] = newSources[sectionY];
		}
		
		OnThread.runOnMainThread(
				() -> {
					for (Collection<LightInfo> newSource : sources) {
						for (LightInfo light : newSource) {
							manager.updateLight(light.light(), light.pos());
						}
					}
				}, 10
		);
	}
	
	@Inject(at = @At("TAIL"), method = "replaceWithPacketData")
	public void postReplace(FriendlyByteBuf pBuffer, CompoundTag pTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> p_187974_, CallbackInfo ci) {
		// TODO: I'd love to put this on another thread
		try {
			if (!wasLoaded) {
				fill((LevelChunk) (Object) this, this.sources);
				wasLoaded = true;
				ChunkLoadState.firstBatch = false;
			} else {
				Threading.chunkLightLoader.addAction(() -> fill((LevelChunk) (Object) this, this.sources));
			}
		} catch (Throwable err) {
			err.printStackTrace();
			if (!FMLEnvironment.production)
				Runtime.getRuntime().exit(-1);
		}
	}
}
