package tfc.tingedlights.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

import java.util.ArrayDeque;
import java.util.Deque;

public class ColoredBlockLightingEngine extends BlockLightEngine {
	Light type;
	BlockGetter level;
	
	public ColoredBlockLightingEngine(Light type, LightChunkGetter lightChunk, BlockGetter level) {
		super(lightChunk);
		this.type = type;
		this.level = level;
	}
	
	BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	
	Deque<Runnable> events = new ArrayDeque<>();
	
	@Override
	public int getLightEmission(long pLevelPos) {
		int i = BlockPos.getX(pLevelPos);
		int j = BlockPos.getY(pLevelPos);
		int k = BlockPos.getZ(pLevelPos);
		BlockGetter blockgetter = this.chunkSource.getChunkForLighting(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(k));
		if (blockgetter == null) return 0;
		
		pos.set(i, j, k);
		BlockState state = blockgetter.getBlockState(pos);
		TingedLightsBlockAttachments attachments = (TingedLightsBlockAttachments) state.getBlock();
		Light light = attachments.createLight(state, blockgetter, pos);
		if (light == null) return 0;
		if (light.equals(type)) return attachments.getBrightness(state, blockgetter, pos);
		return 0;
	}
	
	@Override
	public int runUpdates(int p_75648_, boolean p_75649_, boolean p_75650_) {
		if (!events.isEmpty()) {
			for (int i = 0; i < Minecraft.getInstance().options.renderDistance * Minecraft.getInstance().options.renderDistance; i++) {
				events.pop().run();
			}
		}
		return super.runUpdates(p_75648_, p_75649_, p_75650_);
	}
	
	@Override
	public void updateSectionStatus(SectionPos pPos, boolean pIsEmpty) {
		super.updateSectionStatus(pPos, pIsEmpty);
		
		events.add(() -> {
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						if (x != 15 && y != 15 && x != 0 && y != 0 && z != 0 && z != 15)
							z = 14;
						
						pos.set(pPos.minBlockX() + x, pPos.minBlockY() + y, pPos.minBlockZ() + z);
						
						if (x == 0) checkNode(pos.asLong());
						if (x == 15) checkNode(pos.asLong());
						
						if (z == 0) checkNode(pos.asLong());
						if (z == 15) checkNode(pos.asLong());
						
						if (y == 0) checkNode(pos.asLong());
						if (y == 15) checkNode(pos.asLong());
					}
				}
			}
		});
	}
}
