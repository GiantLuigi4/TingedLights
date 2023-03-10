package tfc.tingedlights.data.struct;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.antlr.v4.runtime.misc.Array2DHashSet;
import tfc.tingedlights.data.FastUtil;

import java.util.Collection;

public class LightingUpdates {
	public final Collection<LightNode>[] freshNodes = new Collection[15];
	public Collection<LightNode>[] newNodes = new Collection[15];
	public Collection<LightNode>[] addedNodes = new Collection[15];
	
	public LightingUpdates() {
		for (int i = 0; i < freshNodes.length; i++) {
//			freshNodes[i] = new Array2DHashSet<>(FastUtil.nodeEquality);
			freshNodes[i] = new ObjectOpenCustomHashSet<>(FastUtil.nodeStrategy);
//			freshNodes[i] = new ObjectArrayList<>();
//			newNodes[i] = new ObjectOpenCustomHashSet<>(FastUtil.nodeStrategy);
			newNodes[i] = new ObjectArrayList<>();
//			addedNodes[i] = new ObjectOpenCustomHashSet<>(FastUtil.nodeStrategy);
			addedNodes[i] = new ObjectArrayList<>();
		}
	}
	
	boolean natural = true;
	boolean passed = false;
	
	public void tick() {
		if (!passed) {
			for (Collection<LightNode> newNode : newNodes) {
				if (!newNode.isEmpty()) {
					passed = true;
					return;
				}
			}
		}
		if (natural) {
			for (Collection<LightNode> newNode : newNodes) {
				if (!newNode.isEmpty()) {
					return;
				}
			}
			natural = false;
		}
	}
	
	public void addFresh(LightNode node) {
		freshNodes[node.brightness() - 1].add(node);
	}
	
	public void swap() {
		for (int i = 0; i < freshNodes.length; i++) {
			Collection<LightNode> freshNodes = this.freshNodes[i];
			Collection<LightNode> newNodes = this.newNodes[i];
			if (!freshNodes.isEmpty()) {
				synchronized (this.freshNodes) {
					newNodes.addAll(freshNodes);
					freshNodes.clear();
				}
			}
		}
	}
	
	public boolean hasAny() {
		for (Collection<LightNode> newNode : newNodes)
			if (!newNode.isEmpty()) return true;
		return false;
	}
	
	public void removeAll(Collection<LightNode> newlyRemovedNodes) {
		for (int i = 0; i < newNodes.length; i++) {
			Collection<LightNode> newNode = newNodes[i];

//			Collection<LightNode> newNode1 = new Array3dHashSet<>(FastUtil.nodeEquality3D);
			Collection<LightNode> newNode1 = new Array2DHashSet<>(FastUtil.nodeEquality);
			newNode1.addAll(newNode);
			newNode1.removeAll(newlyRemovedNodes);
			
			newNodes[i] = new ObjectArrayList<>(newNode1);
		}
	}
	
	public boolean splitWorkload() {
		int totalRemaining = 0;
		for (int i = 0; i < 15; i++) {
			totalRemaining += newNodes[i].size();
		}
		return totalRemaining >= 30000;
	}
	
	public boolean allowReversal() {
		if (!natural) return false;
		int totalRemaining = 0;
		for (int i = 0; i < 15; i++) {
			totalRemaining += newNodes[i].size();
		}
//		return totalRemaining >= 300000;
		return false;
	}
}
