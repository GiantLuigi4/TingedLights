package tfc.tingedlights.data.struct;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.HashMap;
import java.util.Map;

public class LightLookup {
	Map<Integer, Map<BlockPos, LightBlock>> map = new HashMap<>();
	
	public LightBlock get(BlockPos relativePos) {
		BlockPos clamped = new BlockPos(relativePos.getX() & 15, relativePos.getY() & 15, relativePos.getZ() & 15);
		int sectionY = (int) SectionPos.blockToSection(relativePos.getY());
		Map<BlockPos, LightBlock> theMap = map.get(sectionY);
		if (theMap == null) map.put(sectionY, theMap = new HashMap<>());
		return theMap.get(clamped);
	}
	
	public LightBlock remove(BlockPos relativePos) {
		BlockPos clamped = new BlockPos(relativePos.getX() & 15, relativePos.getY() & 15, relativePos.getZ() & 15);
		int sectionY = (int) SectionPos.blockToSection(relativePos.getY());
		Map<BlockPos, LightBlock> theMap = map.get(sectionY);
		if (theMap == null) map.put(sectionY, theMap = new HashMap<>());
		return theMap.remove(clamped);
	}
	
	public LightBlock put(BlockPos relativePos, LightBlock lightBlock) {
		BlockPos clamped = new BlockPos(relativePos.getX() & 15, relativePos.getY() & 15, relativePos.getZ() & 15);
		int sectionY = (int) SectionPos.blockToSection(relativePos.getY());
		Map<BlockPos, LightBlock> theMap = map.get(sectionY);
		if (theMap == null) map.put(sectionY, theMap = new HashMap<>());
		return theMap.put(clamped, lightBlock);
	}
	
	public void clear() {
		map.clear();
	}
}
