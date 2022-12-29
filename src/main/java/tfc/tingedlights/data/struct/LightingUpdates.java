package tfc.tingedlights.data.struct;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import tfc.tingedlights.data.FastUtil;

import java.util.Collection;
import java.util.Set;

public class LightingUpdates {
	public final Collection<LightNode>[] freshNodes = new Collection[15];
	public Collection<LightNode>[] newNodes = new Collection[15];
	public Collection<LightNode>[] addedNodes = new Collection[15];
	
	public LightingUpdates() {
		for (int i = 0; i < freshNodes.length; i++) {
			freshNodes[i] = new ObjectOpenCustomHashSet<>(FastUtil.nodeStrategy);
//			newNodes[i] = new ObjectOpenCustomHashSet<>(FastUtil.nodeStrategy);
//			newNodes[i] = new ObjectArraySet<>();
			newNodes[i] = new ObjectArrayList<>();
			addedNodes[i] = new ObjectOpenCustomHashSet<>(FastUtil.nodeStrategy);
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
	
	public void removeAll(Set<LightNode> newlyRemovedNodes) {
		for (Collection<LightNode> newNode : newNodes) {
			newNode.removeAll(newlyRemovedNodes);
		}
	}
	
	public boolean splitWorkload() {
		for (int i = 5; i < 15; i++) {
			if (!newNodes[i].isEmpty()) return false;
		}
		
		int totalRemaining = 0;
		for (int i = 0; i <= 5; i++) {
			totalRemaining += newNodes[i].size();
		}
		return totalRemaining <= 10000;
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
