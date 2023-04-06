package tfc.tingedlights.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.BlockLightEngine;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.TingedLightsBlockAttachments;

public class ColoredBlockLightingEngine extends BlockLightEngine {
	Light type;
	BlockGetter level;
	
	public ColoredBlockLightingEngine(Light type, LightChunkGetter lightChunk, BlockGetter level) {
		super(lightChunk);
		this.type = type;
		this.level = level;
	}
	
	BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	
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
}
