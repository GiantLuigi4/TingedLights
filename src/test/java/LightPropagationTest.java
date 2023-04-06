//import it.unimi.dsi.fastutil.Hash;
//import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//
//import java.util.*;
//
//public class LightPropagationTest {
//	private static final Direction[] DIRECTIONS = Direction.values();
//	private static byte BYTE_1 = (byte) 1;
//
//	protected static int comparePos(int v0, int v1) {
//		int absV0 = Math.abs(v0);
//		int absV1 = Math.abs(v1);
//		if (absV0 < absV1)
//			return -1;
//		if (absV0 == absV1)
//			return Integer.compare(v0, v1);
//		return 1;
//	}
//
//	public static LightChunk dummyPropagation() {
//		LightChunk chunk = new LightChunk(null, null);
////		LightNode srcNode = new LightNode(
////				chunk,
////				new HashSet<>(),
////				new HashMap<>(),
////				new Light(
////						Color.fromRGB(255, 128, 232),
////						(byte) 0,
////						new BlockPos(90, -40, 12)
////				),
////				(byte) 15,
////				new BlockPos(90, -40, 12)
////		);
////		srcNode.system().put(srcNode.pos, srcNode);
//
//		Comparator<LightNode> nodeComparator = (node0, node1) -> {
//			int v = Integer.compare(node1.brightness(), node0.brightness());
//			if (v == 0) {
//				if (node0.reference() == null) {
//					if (node1.reference() == null) {
//						return node0.light().compareTo(node1.light());
//					}
//					return 1;
//				} else if (node1.reference() == null) {
//					return -1;
//				}
//
//				int relX0 = node0.pos.getX() - node0.reference().pos.getX();
//				int relX1 = node1.pos.getX() - node1.reference().pos.getX();
//				v = comparePos(relX0, relX1);
//				if (v == 0) {
//					int relY0 = node0.pos.getY() - node0.reference().pos.getY();
//					int relY1 = node1.pos.getY() - node1.reference().pos.getY();
//
//					v = comparePos(relY0, relY1);
//					if (v == 0) {
//						int relZ0 = node0.pos.getZ() - node0.reference().pos.getZ();
//						int relZ1 = node1.pos.getZ() - node1.reference().pos.getZ();
//
//						v = comparePos(relZ0, relZ1);
//					}
//				}
//			}
//			return v;
//		};
//		Set<LightNode> newNodes = new TreeSet<>(nodeComparator);
//		Set<LightNode> addedNodes = new TreeSet<>(nodeComparator);
//		Hash.Strategy<LightNode> nodeStrategy = new Hash.Strategy<LightNode>() {
//			@Override
//			public int hashCode(LightNode o) {
//				if (o == null) return 0;
//				return (
//						((((o.pos.getX() & 127) * 127) +
//								((o.pos.getY() & 127) * 127)) +
//								((o.pos.getZ() & 127)))
//				) * o.brightness();
//			}
//
//			@Override
//			public boolean equals(LightNode a, LightNode b) {
//				if (a == b) return true;
//				if (a == null) return false;
//				if (b == null) return false;
//				return
//						a.pos.getX() == b.pos.getX() &&
//								a.pos.getY() == b.pos.getY() &&
//								a.pos.getZ() == b.pos.getZ()
//						;
//			}
//		};
//		Set<LightNode> finishedNodes = new ObjectOpenCustomHashSet<>(600, nodeStrategy);
////		newNodes.add(srcNode);
//		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
//
////		chunk.addNode(srcNode.clampedPos(mutableBlockPos).immutable(), srcNode);
//
//		while (!newNodes.isEmpty()) {
//			for (LightNode node : newNodes) {
//				finishedNodes.add(node);
//
//				for (Direction direction : DIRECTIONS) {
//					BlockPos immut = new BlockPos(node.pos.getX() + direction.getStepX(), node.pos.getY() + direction.getStepY(), node.pos.getZ() + direction.getStepZ());
//
//					if (node.system().containsKey(immut)) {
//						continue;
//					}
//
//					node.clampedPos(mutableBlockPos);
//					int rx = immut.getX() & 15;
//					int ry = immut.getY();
//					int rz = immut.getZ() & 15;
//					if (rx == 15 && mutableBlockPos.getX() == 0) continue;
//					if (rx == 0 && mutableBlockPos.getX() == 15) continue;
//
//					if (rz == 15 && mutableBlockPos.getZ() == 0) continue;
//					if (rz == 0 && mutableBlockPos.getZ() == 15) continue;
//
//					if (node.brightness() != BYTE_1) {
//						LightNode ref = node.reference();
//						if (ref == null) ref = node;
//						LightNode node1 = new LightNode(
//								chunk,
//								ref.source,
//								(byte) (node.brightness() - BYTE_1),
//								immut
//						);
//						chunk.addNode(new BlockPos(rx, ry, rz), node);
//						node.addChild(node1);
//						addedNodes.add(node1);
//					}
//				}
//			}
//
//			if (finishedNodes.size() == newNodes.size())
//				newNodes.clear();
//			else
//				newNodes.removeAll(finishedNodes);
//
//			finishedNodes.clear();
//			if (newNodes.size() == 0) {
//				Set<LightNode> temp = addedNodes;
//				addedNodes = newNodes;
//				newNodes = temp;
//			} else {
//				newNodes.addAll(addedNodes);
//				addedNodes.clear();
//			}
//		}
//
//		return chunk;
//	}
//}
