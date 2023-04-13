package tfc.tingedlights.util.asm;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class HookPatcher {
	String[] target;
	List<Patch> patches;
	
	public HookPatcher(String[] target, List<Patch> patches) {
		this.target = target;
		this.patches = patches;
	}
	
	public boolean canApply(ClassNode node) {
		for (String s : target) {
			if (node.name.equals(s.substring(1, s.length() - 1))) {
				return true;
			}
		}
		
		return false;
	}
	
	public int apply(ClassNode node) {
		int countHits = 0;
		for (Patch patch : patches)
			countHits += patch.apply(node);
		return countHits;
	}
}
