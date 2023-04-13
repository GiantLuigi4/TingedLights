package tfc.tingedlights.util.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class Patch {
	protected final String srcClass;
	
	public Patch(String src) {
		this.srcClass = src;
	}
	
	public int apply(ClassNode node) {
		int countHits = 0;
		for (MethodNode method : node.methods)
			countHits += apply(node, method);
		return countHits;
	}
	
	public abstract int apply(ClassNode classNode, MethodNode method);
}
