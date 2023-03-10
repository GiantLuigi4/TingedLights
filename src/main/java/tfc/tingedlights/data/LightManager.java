package tfc.tingedlights.data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import tfc.tingedlights.api.data.Light;
import tfc.tingedlights.data.access.IHoldColoredLights;
import tfc.tingedlights.data.struct.LightBlock;
import tfc.tingedlights.data.struct.LightChunk;
import tfc.tingedlights.data.struct.LightNode;
import tfc.tingedlights.data.struct.LightingUpdates;

import java.lang.ref.WeakReference;
import java.util.*;

public class LightManager {
	WeakReference<Level> level;
	HashMap<ChunkPos, LightChunk> lightChunkHashMap = new HashMap<>();
	
	private static final Direction[] DIRECTIONS = Direction.values();
	
	// TODO: priority nodes (nodes near the camera)
	// TODO: use distance based lights for far away chunks
	
	protected final LightingUpdates priorityUpdates = new LightingUpdates();
	protected final LightingUpdates regularUpdates = new LightingUpdates();
	protected final Collection<LightBlock> dirtyBlocks = new ArrayList<>();
	
	protected final Collection<LightNode> newlyRemovedNodes = new ArrayList<>();
	protected final Collection<Light> addedLights = new ArrayList<>();
	
	protected final Collection<LightNode> updatedBlocks = new ArrayList<>();
	
	protected int distance(ChunkPos pos0, ChunkPos pos1) {
		return Math.abs(pos0.x - pos1.x) + Math.abs(pos0.z - pos1.z);
	}
	
	ChunkPos currentPos = null;
	
	protected void handleLightAddition() {
		synchronized (addedLights) {
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
							new Array2DHashSet<>(),
							new HashMap<>(),
							light,
							light.lightValue(),
							light.position()
					);
					Collection<LightNode> sources = getSources(light.position());
					if (sources != null) {
						if (sources.contains(node)) {
							// TODO: figure out where a CME comes from here?
							// I don't see how it's possible for it to happen
							for (LightNode source : sources.toArray(new LightNode[0])) {
								if (source.equals(node)) {
									source.remove();
								}
							}
						}
					}
					node.system().put(node.pos, node);
					BlockPos relative;
					chunk.addNode(relative = node.clampedPos(new BlockPos.MutableBlockPos()).immutable(), node);
					
					addLightNode(node); // TODO: add in bulk?
					dirtyBlocks.add(chunk.getLightInfo(relative));
				}
				addedLights.clear();
				addedLights.addAll(deferFurther);
			}
		}
	}
	
	protected void addLightNode(LightNode node) {
		Vec3 eyePos = Minecraft.getInstance().cameraEntity.getEyePosition(0);
		Vec3i eyeVec3i = new Vec3i(eyePos.x, eyePos.y, eyePos.z);
		ChunkPos entityChunk = Minecraft.getInstance().cameraEntity.chunkPosition();
		
		ChunkAccess access = node.chunk.access.get();
		if (access == null) return;
		ChunkPos ps = access.getPos();
		int distance = Math.abs(ps.x - entityChunk.x) + Math.abs(ps.z - entityChunk.z);
		if (distance > 8) {
			return;
		}
		if (distance > 4) {
			synchronized (regularUpdates.freshNodes) {
				regularUpdates.addFresh(node);
			}
		} else {
			synchronized (priorityUpdates.freshNodes) {
				priorityUpdates.addFresh(node);
			}
		}
		newlyRemovedNodes.remove(node);
	}
	
	public void bulkAddNodes(Collection<LightNode> nodes) {
		Vec3 eyePos = Minecraft.getInstance().cameraEntity.getEyePosition(0);
		Vec3i eyeVec3i = new Vec3i(eyePos.x, eyePos.y, eyePos.z);
		ChunkPos entityChunk = Minecraft.getInstance().cameraEntity.chunkPosition();
		
		synchronized (regularUpdates.freshNodes) {
			synchronized (priorityUpdates.freshNodes) {
				for (LightNode node : nodes) {
					ChunkPos ps = node.chunk.access.get().getPos();
					int distance = Math.abs(ps.x - entityChunk.x) + Math.abs(ps.z - entityChunk.z);
					if (distance > 8) {
						return;
					}
					if (distance > 4) {
						regularUpdates.addFresh(node);
					} else {
						priorityUpdates.addFresh(node);
					}
				}
			}
		}
		newlyRemovedNodes.removeAll(nodes);
	}
	
	// TODO: smth jank, also slow
	protected int propagate(LightingUpdates updates, int maxUpdates, Set<LightNode> finishedNodes, BlockPos.MutableBlockPos mutableBlockPos, boolean priority, Collection<LightNode> updatedPositions, Collection<LightBlock> updatedLightBlocks) {
		updates.tick();
		Random random = new Random();
		int trueMax = maxUpdates;
		int trueCount = 0;
		int addCount = 0;
		int maxPerIter = maxUpdates;
//		if (updates.splitWorkload()) maxPerIter /= 15;
		maxPerIter /= 15;
		// true: more stutter, but more even framerate during propagation
		// false: "more" performance, less stutter, but when switching between layers, it lags a lot for a bit
		boolean forwards = updates.allowReversal(); // helps reduce perceived lag, at the cost of recomputing light values several times
		for (int i = forwards ? 0 : 14; forwards ? (i < 15) : (i >= 0); i = i + (forwards ? 1 : -1)) {
			maxUpdates = maxPerIter;
//			maxUpdates += addCount;
			
			int updateCount = 0;
			Collection<LightNode> newNodes = updates.newNodes[i];
			Collection<LightNode> addedNodes = updates.addedNodes[i];
			
			nodeLoop:
			for (LightNode node : newNodes) {
				finishedNodes.add(node);
				
				try {
					for (Direction direction : DIRECTIONS) {
						BlockPos immut = new BlockPos(node.pos.getX() + direction.getStepX(), node.pos.getY() + direction.getStepY(), node.pos.getZ() + direction.getStepZ());
						
						LightNode nd = node.system().get(immut);
						if (nd != null) {
							byte old = nd.brightness();
							byte current = (byte) (node.brightness() - dimmingAmount(nd.pos, nd.clampedPos(mutableBlockPos), nd.chunk, direction));
							if (current > old) {
								nd.setBrightness(current);
								addedNodes.add(nd);
								
								// TODO: debug?
								// for some reason, if updatedLightBlocks.add() is in the if statement, some blocks on the edges of chunks don't refresh
								updatedPositions.add(node);
							}
							updatedLightBlocks.add(node.chunk.getLightInfo(immut));
							continue;
						}
						
						node.clampedPos(mutableBlockPos);
						int rx = immut.getX() & 15;
						int ry = immut.getY();
						int rz = immut.getZ() & 15;
						
						LightChunk chunk = node.chunk;
						if (chunk == null) continue; // TODO: queue light propagation
						if (rx == 15 && mutableBlockPos.getX() == 0)
							chunk = chunk.south();
						if (rx == 0 && mutableBlockPos.getX() == 15)
							chunk = chunk.north();
						
						if (rz == 15 && mutableBlockPos.getZ() == 0)
							chunk = chunk.west();
						if (rz == 0 && mutableBlockPos.getZ() == 15)
							chunk = chunk.east();
						
						if (chunk == null) continue; // TODO: queue light propagation
						if (node.brightness() != (byte) 1) {
							LightNode ref = node.reference();
							if (ref == null) ref = node;
							BlockPos relativePos = new BlockPos(rx, ry, rz);
							ChunkAccess access = chunk.access.get();
							if (access == null) continue;
							
							BlockPos newLightPos = new BlockPos(
									access.getPos().getBlockX(rx),
									ry,
									access.getPos().getBlockZ(rz)
							);
							// calc light
							byte val = (byte) (node.brightness() - dimmingAmount(newLightPos, relativePos, chunk, direction));
							if (val < 1) continue;
							// create node
							LightNode node1 = new LightNode(
									chunk, ref.source,
									val, newLightPos
							);
							// mark for update&propagation
							if (chunk.addNode(relativePos, node1)) {
								updatedPositions.add(node);
								updatedLightBlocks.add(node.chunk.getLightInfo(immut));
								
								node.addChild(node1);
								addedNodes.add(node1);
							}
						}
					}
				} catch (Throwable err) {
					throw new RuntimeException(err);
				}
				
				updateCount++;
				if (updateCount > maxUpdates) break nodeLoop;
			}
			
			// if the every node in the list has been propagated, there is no point in removing the nodes one by one
			// instead, the list can be cleared on the spot
			if (finishedNodes.size() == newNodes.size()) newNodes.clear();
			else newNodes.removeAll(finishedNodes);
			
			finishedNodes.clear();
			
			if (i != 0) {
				newNodes = updates.newNodes[i - 1];
//				newNodes = updates.newNodes[random.nextInt(15)];
				
				// shift the node to the next list of lights to propagate
				newNodes.addAll(addedNodes);
				addedNodes.clear();
			}
			
			// calculate the number of updates to carry to the next light value
			addCount = maxUpdates - updateCount;
			if (addCount < 0) addCount = 0;
			// increase the return value
			trueCount += updateCount;
			// reset max
			maxUpdates = trueMax;
			
			if (updateCount > maxUpdates) break;
		}
		
		return trueCount;
	}
	
	protected void checkPlayerMotion() {
		// TODO: deal with mods that render the world multiple times
		if (currentPos == null || !currentPos.equals(Minecraft.getInstance().cameraEntity.chunkPosition())) {
			currentPos = Minecraft.getInstance().cameraEntity.chunkPosition();
			for (int xO = -8; xO <= 8; xO++) {
				for (int zO = -8; zO <= 8; zO++) {
					ChunkPos ps = new ChunkPos(
							currentPos.x + xO,
							currentPos.z + zO
					);
					LightChunk chunk = lightChunkHashMap.get(ps);
					if (chunk == null) continue;
					int d = distance(ps, currentPos);
					
					ChunkAccess access = chunk.access.get();
					if (access == null) continue;
					
					if (d > 9) {
						chunk.clear();
					} else if (d == 8) {
						// TODO: mark edges dirty (somehow)
						for (Collection<Light> source : ((IHoldColoredLights) access).getSources()) {
							if (source == null)
								continue;
							loopLight:
							for (Light light : source) {
								Collection<LightNode> blockSources = getSources(light.position());
								if (blockSources != null) {
									for (LightNode node : blockSources) {
										if (node.light().equals(light)) {
											regularUpdates.addFresh(node);
											continue loopLight;
										}
									}
								}
								
								addLight(light);
							}
						}
					}
				}
			}
		}
	}
	
	public void runUpdate() {
//		Collection<LightNode> freshNodes = this.freshNodes;
//		Set<LightNode> newNodes = this.newNodes;
//		Set<LightNode> freshPriorityNodes = this.freshPriorityNodes;
//		Set<LightNode> addedPriorityNodes = this.addedPriorityNodes;
//		Set<LightNode> addedNodes = this.addedNodes;
//		Set<LightNode> priorityNodes = this.priorityNodes;
//
//		Collection<LightNode> newlyRemovedNodes = this.newlyRemovedNodes;
//		Collection<LightBlock> dirtyBlocks = this.dirtyBlocks;
		try {
			try {
				priorityUpdates.swap();
				regularUpdates.swap();
			} catch (Throwable ignored) {
			}
			
			int maxPerSource = 3000; // TODO: find a more exact calculation
			int expOut = maxPerSource * 8;
			int maxUpdates = expOut;

//			Set<LightNode> finishedNodes = new ObjectOpenCustomHashSet<>(600, FastUtil.nodeStrategy);
			Set<LightNode> finishedNodes = new Array2DHashSet<>(FastUtil.nodeEquality);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

//			Collection<LightNode> updatedPositions = new ObjectOpenCustomHashSet<>(600, FastUtil.nodeStrategy);
			Collection<LightNode> updatedPositions = new ArrayList<>();
			Collection<LightBlock> updatedBlocks = new Array2DHashSet<>();
			int maxForPrior = maxUpdates;
			if (regularUpdates.hasAny()) maxForPrior /= 1.5;
			
			try {
				if (!newlyRemovedNodes.isEmpty()) {
					synchronized (newlyRemovedNodes) {
						priorityUpdates.removeAll(newlyRemovedNodes);
						regularUpdates.removeAll(newlyRemovedNodes);
					}
				}
			} catch (Throwable ignored) {
			}
			
			maxUpdates -= this.propagate(priorityUpdates, maxForPrior, finishedNodes, mutableBlockPos, true, updatedPositions, updatedBlocks);
			maxUpdates /= 32;
			if (maxUpdates > 0)
				this.propagate(regularUpdates, maxUpdates, finishedNodes, mutableBlockPos, false, updatedPositions, updatedBlocks);
			
			try {
				if (!newlyRemovedNodes.isEmpty()) {
					synchronized (newlyRemovedNodes) {
						priorityUpdates.removeAll(newlyRemovedNodes);
						regularUpdates.removeAll(newlyRemovedNodes);
						
						// TODO: testing
						newlyRemovedNodes.clear();
					}
				}
			} catch (Throwable ignored) {
			}
			
			synchronized (dirtyBlocks) {
				for (LightBlock updatedLightBlock : updatedBlocks) {
					if (updatedLightBlock != null)
						updatedLightBlock.computeColor();
				}
			}
			
			synchronized (this.updatedBlocks) {
				this.updatedBlocks.addAll(updatedPositions);
			}
			
			if (!regularUpdates.hasAny() && !regularUpdates.hasAny())
				Thread.sleep(1);
		} catch (Throwable ignored) {
			ignored.printStackTrace();
		}
	}
	
	public static Thread createThread(WeakReference<LightManager> managerWeakReference) {
		Thread propagationHandler = new Thread(() -> {
			while (true) {
				LightManager manager = managerWeakReference.get();
				if (manager == null) return;
				manager.runUpdate();
			}
		});
		return propagationHandler;
	}
	
	//	Thread propagationHandler;
	public LightManager(Level level) {
		this.level = new WeakReference<>(level);
//		propagationHandler = createThread(new WeakReference<>(this));
//		propagationHandler.setDaemon(true);
//		propagationHandler.start();
	}
	
	ChunkPos playerPos = new ChunkPos(0, 0);
	
	// TODO: this algorithm is buggy
	// TODO: multi threading would be neat
	
	// TODO: mark neighbor chunks dirty
	public void tick(int maxUpdates) {
		playerPos = Minecraft.getInstance().cameraEntity.chunkPosition();
		
		handleLightAddition();
		checkPlayerMotion();
		
		runUpdate();
		
		synchronized (this.updatedBlocks) {
			for (LightNode updatedBlock : updatedBlocks)
				if (updatedBlock != null)
					updatedBlock.chunk.markDirty(updatedBlock);
			updatedBlocks.clear();
		}
		try {
			if (!dirtyBlocks.isEmpty()) {
				synchronized (dirtyBlocks) {
					for (LightBlock dirtyBlock : dirtyBlocks) {
						if (dirtyBlock == null) continue;
						dirtyBlock.computeColor();
					}
					dirtyBlocks.clear();
				}
			}
		} catch (Throwable ignored) {
		}
	}
	
	protected int dimmingAmount(BlockPos actualPos, BlockPos relativePos, LightChunk chunk, Direction direction) {
		ChunkAccess access = chunk.access.get();
		if (access == null) return 15;
		BlockState state = access.getBlockState(relativePos);
		Level level = this.level.get();
		if (level == null) return 15; // don't think this should be an issue, but might as well
		int block = state.getLightBlock(level, actualPos);
		
		return block + 1;
	}
	
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
		synchronized (addedLights) {
			addedLights.add(light);
		}
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
		bulkAddNodes(toUpdate);
		synchronized (newlyRemovedNodes) {
			newlyRemovedNodes.addAll(toRemove);
		}
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
				bulkAddNodes(nodes);
			}
		}
	}
	
	public Color getColor(BlockPos pos, boolean allowNull) {
		LightChunk chunk = lightChunkHashMap.get(new ChunkPos(pos));
		if (chunk == null) return allowNull ? null : Color.BLACK;
		
		ChunkAccess access = chunk.access.get();
		if (access == null) return allowNull ? null : Color.BLACK;
		// TODO: this could use some cleanup LOL
		if (distance(playerPos, access.getPos()) > 8) {
//			if (true) return Color.BLACK;
			// TODO: optimize this?
			Level level = this.level.get();
			float[] out = new float[]{0, 0, 0};
			LightChunk north = chunk.north();
			LightChunk south = chunk.south();
			LightChunk east = chunk.east();
			LightChunk west = chunk.west();
			LightChunk[] lightChunks0 = new LightChunk[]{
					north == null ? null : north.east(),
					north == null ? null : north.west(),
					north, east,
					south == null ? null : south.east(),
					south == null ? null : south.west(),
					south, west,
					chunk
			};
			LightChunk[] lightChunks1 = new LightChunk[]{
					north, south,
					east, west,
					chunk
			};
			int sec = (int) SectionPos.blockToSection(pos.getY());
			int[] sections = new int[]{sec - 1, sec, sec + 1};
			for (int section : sections) {
				LightChunk[] lightChunks = sec == section ? lightChunks0 : lightChunks1;
				for (LightChunk lightChunk : lightChunks) {
					if (lightChunk == null) continue;
					ChunkAccess access1 = lightChunk.access.get();
					if (access1 == null) continue;
					if (section < access1.getMinSection() || section > access1.getMaxSection()) continue;
					int sectionLook = access1.getSectionIndexFromSectionY(section);
					if (access1 instanceof IHoldColoredLights coloredLightHolder) {
						// TODO: is there a better way to deal with CMEs?
						try {
							for (Light light : coloredLightHolder.getSources()[sectionLook].toArray(new Light[0])) {
								int d = light.position().distManhattan(pos);
								if (d > light.lightValue()) continue;
								
								d = light.lightValue() - d;
								if (d <= 0) continue;
								
								Color color = light.getColor((byte) d);
								out[0] = Math.max(color.r(), out[0]);
								out[1] = Math.max(color.g(), out[1]);
								out[2] = Math.max(color.b(), out[2]);
							}
						} catch (Throwable ignored) {
						}
					}
				}
			}
			return new Color(out[0], out[1], out[2]);
		}
		
		// TODO: lessen allocation?
//		posRender.set(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		BlockPos posRender = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
		LightBlock lb = chunk.getLightInfo(posRender);
		if (lb == null) return allowNull ? null : Color.BLACK;
		return lb.getColor();
	}
	
	public Color getColor(BlockPos pos) {
		return getColor(pos, false);
	}
	
	public void close() {
		System.out.println("closed");
//		// yes
//		for (int i = 0; i < 10; i++) {
//			try {
//				propagationHandler.stop();
//				return;
//			} catch (Throwable ignored) {
//				try {
//					propagationHandler.suspend();
//					return;
//				} catch (Throwable ignored1) {
//					try {
//						propagationHandler.interrupt();
//						return;
//					} catch (Throwable ignored2) {
//						try {
//							propagationHandler.stop();
//							return;
//						} catch (Throwable ignored3) {
//						}
//					}
//				}
//			}
//		}
	}
}
