package tfc.tingedlights.data;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.struct.LightBlock;
import tfc.tingedlights.data.struct.LightChunk;
import tfc.tingedlights.data.struct.LightNode;

import java.util.*;

public class LightManager {
	Level level;
	HashMap<ChunkPos, LightChunk> lightChunkHashMap = new HashMap<>();
	
	public LightManager(Level level) {
		this.level = level;
	}
	
	private static final Direction[] DIRECTIONS = Direction.values();
	
	// TODO: priority nodes (nodes near the camera)
	// TODO: use distance based lights for far away chunks
	Set<LightNode> priorityNodes = new TreeSet<>(nodeComparator);
	Set<LightNode> addedPriorityNodes = new TreeSet<>(nodeComparator);
	Set<LightNode> newNodes = new TreeSet<>(nodeComparator);
	Set<LightNode> addedNodes = new TreeSet<>(nodeComparator);
	
	Set<LightBlock> dirtyBlocks = new HashSet<>();
	
	Set<Light> addedLights = new HashSet<>();
	
	protected void handleLightAddition() {
		if (!addedLights.isEmpty()) {
			Set<Light> deferFurther = new HashSet<>();
			/*
			 * add lights to the light manager
			 * deferred to avoid potential CMEs, as well as because lights can sometimes be added too early
			 */
			for (Light light : addedLights) {
				LightChunk chunk = lightChunkHashMap.get(new ChunkPos(light.position()));
				if (chunk == null) {
					deferFurther.add(light);
					continue;
				}
				LightNode node = new LightNode(
						chunk,
						new HashSet<>(),
						new HashMap<>(),
						light,
						light.lightValue(),
						light.position()
				);
				Collection<LightNode> sources = getSources(light.position());
				if (sources != null) {
					if (sources.contains(node)) {
						for (LightNode source : sources) {
							if (source.equals(node)) {
								source.remove();
							}
						}
					}
				}
				node.system().put(node.pos, node);
				BlockPos relative;
				chunk.addNode(relative = node.clampedPos(new BlockPos.MutableBlockPos()).immutable(), node);
				
				addLightNode(node);
				dirtyBlocks.add(chunk.getLightInfo(relative));
			}
			addedLights.clear();
			addedLights.addAll(deferFurther);
		}
	}
	
	protected void addLightNode(LightNode node) {
		Vec3 eyePos = Minecraft.getInstance().cameraEntity.getEyePosition(0);
		Vec3i eyeVec3i = new Vec3i(eyePos.x, eyePos.y, eyePos.z);
		
		if (node.pos.distManhattan(eyeVec3i) > 250) {
			newNodes.add(node);
		} else {
			priorityNodes.add(node);
		}
	}
	
	protected int propagate(Set<LightNode> newNodes, Set<LightNode> addedNodes, int maxUpdates, Set<LightNode> finishedNodes, BlockPos.MutableBlockPos mutableBlockPos, boolean priority) {
		int updates = 0;
		int trueMax = maxUpdates;
		while (!newNodes.isEmpty()) {
			nodeLoop:
			for (LightNode node : newNodes) {
				finishedNodes.add(node);
				
				for (Direction direction : DIRECTIONS) {
					BlockPos immut = new BlockPos(node.pos.getX() + direction.getStepX(), node.pos.getY() + direction.getStepY(), node.pos.getZ() + direction.getStepZ());
					
					LightNode nd = node.system().get(immut);
					if (nd != null) {
						byte old = nd.brightness();
						byte current = (byte) (node.brightness() - dimmingAmount(level, nd.pos, nd.clampedPos(mutableBlockPos), nd.chunk, direction));
						if (current > old) {
							nd.setBrightness(current);
							addedNodes.add(nd);
						}
						// TODO: update brightness
						continue;
					}
					
					node.clampedPos(mutableBlockPos);
					int rx = immut.getX() & 15;
					int ry = immut.getY();
					int rz = immut.getZ() & 15;
					// TODO: deal with neighbor chunks
					LightChunk chunk = node.chunk;
					if (chunk == null) continue; // TODO: queue light propagation
					if (rx == 15 && mutableBlockPos.getX() == 0) chunk = chunk.south();
					if (rx == 0 && mutableBlockPos.getX() == 15) chunk = chunk.north();
					
					if (rz == 15 && mutableBlockPos.getZ() == 0) chunk = chunk.west();
					if (rz == 0 && mutableBlockPos.getZ() == 15) chunk = chunk.east();
					
					if (chunk == null) continue; // TODO: queue light propagation
					if (node.brightness() != (byte) 1) {
						LightNode ref = node.reference();
						if (ref == null) ref = node;
						BlockPos relativePos = new BlockPos(rx, ry, rz);
						BlockPos newLightPos = new BlockPos(
								chunk.access.getPos().getBlockX(rx),
								ry,
								chunk.access.getPos().getBlockZ(rz)
						);
						byte val = (byte) (node.brightness() - dimmingAmount(level, newLightPos, relativePos, chunk, direction));
						if (val < 1) continue;
						LightNode node1 = new LightNode(
								chunk, ref.source,
								val, newLightPos
						);
						if (chunk.addNode(relativePos, node1)) {
							node.addChild(node1);
							addedNodes.add(node1);
							
							dirtyBlocks.add(chunk.getLightInfo(relativePos));
						}
					}
				}
				
				updates++;
				if (updates > maxUpdates) break nodeLoop;
			}
			
			maxUpdates = trueMax;
			int sz = newNodes.size();
			if (finishedNodes.size() == newNodes.size()) newNodes.clear();
			else newNodes.removeAll(finishedNodes);
			if (sz == newNodes.size()) {
				// TODO: fix the removeAll call
				maxUpdates = Integer.MAX_VALUE;
			}
//			newNodes.clear();
			finishedNodes.clear();
			
			if (newNodes.size() == 0) {
				Set<LightNode> temp = addedNodes;
				addedNodes = newNodes;
				newNodes = temp;
			} else {
				newNodes.addAll(addedNodes);
				addedNodes.clear();
			}
			
			if (updates > maxUpdates) break;
		}
		
		if (priority) {
			this.priorityNodes = newNodes;
			this.addedPriorityNodes = addedNodes;
		} else {
			this.newNodes = newNodes;
			this.addedNodes = addedNodes;
		}
		
		return updates;
	}
	
	// TODO: this algorithm is buggy
	// TODO: multi threading would be neat
	
	// TODO: mark neighbor chunks dirty
	public void tick(int maxUpdates) {
		handleLightAddition();
		
		if (maxUpdates == -1) {
			int maxPerSource = 3000; // TODO: find a more exact calculation
//			maxUpdates = (priorityNodes.size() + newNodes.size()) * maxPerSource;
			int expOut = maxPerSource * 32;
//			if (maxUpdates > expOut) {
////				maxUpdates -= expOut;
////				maxUpdates = (int) (1d / Math.sqrt(1d / maxUpdates));
////				maxUpdates += expOut;
//				maxUpdates = expOut;
//			}
			maxUpdates = expOut;
		}
		
		Set<LightNode> finishedNodes = new ObjectOpenCustomHashSet<>(600, nodeStrategy);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		
		int maxForPrior = maxUpdates;
		if (newNodes.size() != 0) maxForPrior /= 1.5;
		maxUpdates -= propagate(priorityNodes, addedPriorityNodes, maxForPrior, finishedNodes, mutableBlockPos, true);
		maxUpdates /= 8;
		if (maxUpdates > 0) propagate(newNodes, addedNodes, maxUpdates, finishedNodes, mutableBlockPos, false);
		
		for (LightBlock dirtyBlock : dirtyBlocks) dirtyBlock.computeColor();
		dirtyBlocks.clear();
	}
	
	private int dimmingAmount(Level level, BlockPos actualPos, BlockPos relativePos, LightChunk chunk, Direction direction) {
		BlockState state = chunk.access.getBlockState(relativePos);
		int block = state.getLightBlock(level, actualPos);
		
		return block + 1;
	}
	
	/* FastUtil */
	protected static int comparePos(int v0, int v1) {
		int absV0 = Math.abs(v0);
		int absV1 = Math.abs(v1);
		if (absV0 < absV1)
			return -1;
		if (absV0 == absV1)
			return Integer.compare(v0, v1);
		return 1;
	}
	
	private static final Comparator<LightNode> nodeComparator = (node0, node1) -> {
		int v = Integer.compare(node1.brightness(), node0.brightness());
		if (v == 0) {
			if (node0.reference() == null) {
				if (node1.reference() == null) {
					return node0.light().compareTo(node1.light());
				}
				return 1;
			} else if (node1.reference() == null) {
				return -1;
			}
			
			int relX0 = node0.pos.getX() - node0.reference().pos.getX();
			int relX1 = node1.pos.getX() - node1.reference().pos.getX();
			v = comparePos(relX0, relX1);
			if (v == 0) {
				int relY0 = node0.pos.getY() - node0.reference().pos.getY();
				int relY1 = node1.pos.getY() - node1.reference().pos.getY();
				
				v = comparePos(relY0, relY1);
				if (v == 0) {
					int relZ0 = node0.pos.getZ() - node0.reference().pos.getZ();
					int relZ1 = node1.pos.getZ() - node1.reference().pos.getZ();
					
					v = comparePos(relZ0, relZ1);
					
					if (v == 0) {
						v = node0.pos.compareTo(node1.pos);
					}
				}
			}
		}
		return v;
	};
	
	private static final Hash.Strategy<LightNode> nodeStrategy = new Hash.Strategy<LightNode>() {
		@Override
		public int hashCode(LightNode o) {
			if (o == null) return 0;
			return (
					((((o.pos.getX() & 127) * 127) +
							((o.pos.getY() & 127) * 127)) +
							((o.pos.getZ() & 127)))
			) * o.brightness();
		}
		
		@Override
		public boolean equals(LightNode a, LightNode b) {
			if (a == b) return true;
			if (a == null) return false;
			if (b == null) return false;
			return
					a.pos.getX() == b.pos.getX() &&
							a.pos.getY() == b.pos.getY() &&
							a.pos.getZ() == b.pos.getZ() &&
							a.light().equals(b.light())
					;
		}
	};
	/* end FastUtil */
	
	private static final int[][] OFFSETS = new int[][]{
			new int[]{0, 1},
			new int[]{0, -1},
			new int[]{1, 0},
			new int[]{-1, 0}
	};
	
	public void loadChunk(ChunkAccess access) {
		ChunkPos pos = access.getPos();
		LightChunk newChunk = new LightChunk(access, access.getPos());
		for (int[] offset : OFFSETS) {
			LightChunk neighbor = lightChunkHashMap.get(new ChunkPos(pos.x + offset[0], pos.z + offset[1]));
			if (neighbor != null) {
				neighbor.loadChunk(newChunk);
				newChunk.loadChunk(neighbor);
			}
		}
		lightChunkHashMap.put(pos, newChunk);
	}
	
	public void unloadChunk(ChunkPos pos) {
		LightChunk chunk = lightChunkHashMap.get(pos);
		chunk.onUnload();
	}
	
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
	private final BlockPos.MutableBlockPos posRender = new BlockPos.MutableBlockPos();
	
	public void addLight(Light light) {
		addedLights.add(light);
	}
	
	public Collection<LightNode> getSources(BlockPos pos) {
		LightChunk chunk = lightChunkHashMap.get(new ChunkPos(pos));
		// TODO: lessen allocation?
//		posRender.set(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		BlockPos posRender = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		if (chunk == null) return null;
		return chunk.getNodesAt(posRender);
	}
	
	public void removeLights(BlockPos pPos) {
		Collection<LightNode> sources = getSources(pPos);
		if (sources == null) return;
		for (LightNode source : sources.toArray(new LightNode[0])) {
			removeNode(source);
		}
	}
	
	public void removeNode(LightNode source) {
		HashSet<LightNode> toUpdate = new HashSet<>();
		HashSet<LightNode> toRemove = new HashSet<>();
		source.system().forEach((pos, node) -> {
			if (node.brightness() == source.brightness()) {
				toUpdate.add(node);
			} else if (node.brightness() < source.brightness()) {
				toRemove.add(node);
			}
		});
		toUpdate.remove(source);
		toUpdate.remove(source);
		toRemove.add(source);
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
		for (LightNode node : toRemove) {
			node.remove();
			// TODO: optimize?
			LightBlock lb = node.chunk.getLightInfo(node.clampedPos(blockPos).immutable());
			if (lb != null) dirtyBlocks.add(lb);
		}
		newNodes.addAll(toUpdate);
		newNodes.removeAll(toRemove);
	}
	
	// TODO: favor light nodes that have the same brightness as the light
	public void removeLight(Light light) {
		Collection<LightNode> nodes = getSources(light.position());
		if (nodes == null) return;
		for (LightNode node : nodes.toArray(new LightNode[0])) {
			if (node.light().equals(light)) {
				removeNode(node);
			}
		}
	}
	
	// TODO: favor light nodes that have the same brightness as the light
	public void removeLight(byte value, Light light) {
		Collection<LightNode> nodes = getSources(light.position());
		if (nodes == null) return;
		for (LightNode node : nodes.toArray(new LightNode[0])) {
			if (node.brightness() == value) {
				if (node.light().equals(light)) {
					removeNode(node);
				}
			}
		}
	}
	
	public void updateNeighbors(BlockPos pPos) {
		BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
		for (Direction direction : DIRECTIONS) {
			Collection<LightNode> nodes = getSources(blockPos.setWithOffset(pPos, direction));
			if (nodes != null) {
				newNodes.addAll(nodes);
			}
		}
	}
	
	public Color getColor(BlockPos pos, boolean allowNull) {
		LightChunk chunk = lightChunkHashMap.get(new ChunkPos(pos));
		// TODO: lessen allocation?
//		posRender.set(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		BlockPos posRender = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		if (chunk == null) {
			if (allowNull) {
				return null;
			} else {
				return new Color(0, 0, 0);
			}
		}
		LightBlock lb = chunk.getLightInfo(posRender);
		if (lb == null) {
			if (allowNull) {
				return null;
			} else {
				return new Color(0, 0, 0);
			}
		}
		return lb.getColor();
	}
	
	public Color getColor(BlockPos pos) {
		LightChunk chunk = lightChunkHashMap.get(new ChunkPos(pos));
		// TODO: lessen allocation?
//		posRender.set(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		BlockPos posRender = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		if (chunk == null) return new Color(0, 0, 0);
		LightBlock lb = chunk.getLightInfo(posRender);
		if (lb == null) return new Color(0, 0, 0);
		return lb.getColor();
	}
}
