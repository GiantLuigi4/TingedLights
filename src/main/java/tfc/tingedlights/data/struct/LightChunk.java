package tfc.tingedlights.data.struct;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.tingedlights.data.Color;
import tfc.tingedlights.data.access.IHoldColoredLights;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;

public class LightChunk {
	LightLookup nodes = new LightLookup();
	
	public final WeakReference<ChunkAccess> access;
	ChunkPos pos;
	
	public LightChunk(ChunkAccess access, ChunkPos pos) {
		this.access = new WeakReference<>(access);
		this.pos = pos;
	}
	
	public void unloadChunk(ChunkPos pos) {
		if (pos.x == this.pos.x + 1)
			north = null;
		if (pos.x == this.pos.x - 1)
			south = null;
		if (pos.z == this.pos.z + 1)
			east = null;
		if (pos.z == this.pos.z - 1)
			west = null;
	}
	
	public void loadChunk(LightChunk chunk) {
		ChunkPos pos = chunk.pos;
		if (pos.x == this.pos.x + 1)
			north = chunk;
		if (pos.x == this.pos.x - 1)
			south = chunk;
		if (pos.z == this.pos.z + 1)
			east = chunk;
		if (pos.z == this.pos.z - 1)
			west = chunk;
	}
	
	public LightChunk getChunk(int offset, boolean x) {
		if (x) {
			if (offset == 1)
				return north;
			if (offset == -1)
				return south;
		} else {
			if (offset == 1)
				return east;
			if (offset == -1)
				return west;
		}
		return null;
	}
	
	LightChunk north = null;
	LightChunk south = null;
	LightChunk east = null;
	LightChunk west = null;
	
	public LightChunk north() {
		return north;
	}
	
	public LightChunk south() {
		return south;
	}
	
	public LightChunk east() {
		return east;
	}
	
	public LightChunk west() {
		return west;
	}
	
	public void setNorth(LightChunk north) {
		this.north = north;
	}
	
	public void removeNode(BlockPos relativePos, LightNode nodule) {
		LightBlock nodules = nodes.get(relativePos);
		if (nodules == null) return;
		if (nodules.removeLight(nodule)) {
			if (nodule.reference() == nodule) {
				if (access.get() instanceof IHoldColoredLights iHoldColoredLights) {
					int section = (int) SectionPos.blockToSection(nodule.pos.getY());
					int sectionLook = access.get().getSectionIndex(section);
					iHoldColoredLights.getSources()[sectionLook].remove(nodule.light());
				}
			}
		}
		if (nodules.lights() == 0) nodes.remove(relativePos);
	}
	
	public void markDirty(LightNode node) {
		if (access.get() instanceof LevelChunk chunk) {
			BlockPos pos = node.pos;
			BlockState state = chunk.getBlockState(node.clampedPos(new BlockPos.MutableBlockPos()).immutable());
			BlockState dummyState = Blocks.AIR.defaultBlockState();
			if (state.isAir()) dummyState = Blocks.STONE.defaultBlockState();
			chunk.getLevel().setBlocksDirty(pos, dummyState, state);
		}
	}
	
	public boolean addNode(BlockPos relativePos, LightNode lightNode) {
		LightBlock nodules = getLights(relativePos);
		if (nodules.contains(lightNode))
			return false;
		ChunkAccess access = this.access.get();
		BlockPos minPos = new BlockPos(
				access.getPos().getMinBlockX(),
				access.getMinBuildHeight(),
				access.getPos().getMinBlockZ()
		);
		BlockPos maxPos = new BlockPos(
				access.getPos().getMaxBlockX(),
				access.getMaxBuildHeight(),
				access.getPos().getMaxBlockZ()
		);
		if (lightNode.pos.getX() < minPos.getX()) return false;
		if (lightNode.pos.getY() < minPos.getY()) return false;
		if (lightNode.pos.getZ() < minPos.getZ()) return false;
		if (lightNode.pos.getX() > maxPos.getX()) return false;
		if (lightNode.pos.getY() > maxPos.getY()) return false;
		if (lightNode.pos.getZ() > maxPos.getZ()) return false;
		nodules.addLight(lightNode);
		return true;
	}
	
	public Collection<LightNode> getNodesAt(BlockPos relativePos) {
		LightBlock lightBridge = nodes.get(relativePos);
		if (lightBridge == null) return null;
		return lightBridge.nodes;
	}
	
	public LightBlock getLightInfo(BlockPos relativePos) {
		return nodes.get(relativePos);
	}
	
	public Color getColor(BlockPos relativePos) {
		return nodes.get(relativePos).getColor();
	}
	
	LightBlock getLights(BlockPos pos) {
		// TODO: check performance overhead of computeIfAbsent
		LightBlock nodules = nodes.get(pos);
		if (nodules == null) nodes.put(pos, nodules = new LightBlock(new HashSet<>()));
		return nodules;
	}
	
	public void onUnload() {
		if (north != null) north.unloadChunk(pos);
		if (south != null) south.unloadChunk(pos);
		if (east != null) east.unloadChunk(pos);
		if (west != null) west.unloadChunk(pos);
		north = null;
		south = null;
		east = null;
		west = null;
	}
}
