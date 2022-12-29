package tfc.tingedlights.data;

import it.unimi.dsi.fastutil.Hash;
import tfc.tingedlights.data.struct.LightNode;

import java.util.Comparator;

public class FastUtil {
	/* FastUtil */
	public static int comparePos(int v0, int v1) {
		int absV0 = Math.abs(v0);
		int absV1 = Math.abs(v1);
		if (absV0 < absV1)
			return -1;
		if (absV0 == absV1)
			return Integer.compare(v0, v1);
		return 1;
	}
	
	public static final Comparator<LightNode> nodeComparator = (node0, node1) -> {
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
	
	public static final Hash.Strategy<LightNode> nodeStrategy = new Hash.Strategy<LightNode>() {
		@Override
		public int hashCode(LightNode o) {
			if (o == null) return 0;
			return o.hash;
//			return (
//					((((o.pos.getX() & 127) * 127) +
//							((o.pos.getY() & 127) * 127)) +
//							((o.pos.getZ() & 127)))
//			) * o.brightness();
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
}
