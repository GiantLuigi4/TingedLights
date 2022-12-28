package tfc.tingedlights.data.struct;

import java.util.Set;
import java.util.TreeSet;

public class LightingUpdates {
	protected final Set<LightNode>[] freshNodes = new TreeSet<>(nodeComparator);
	protected Set<LightNode>[] newNodes = new TreeSet<>(nodeComparator);
	protected Set<LightNode>[] addedNodes = new TreeSet<>(nodeComparator);
}
