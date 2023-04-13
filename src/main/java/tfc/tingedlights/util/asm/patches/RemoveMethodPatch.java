package tfc.tingedlights.util.asm.patches;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import tfc.tingedlights.util.asm.Patch;
import tfc.tingedlights.util.asm.struct.template.MethodTargetStruct;

import java.util.ArrayList;

public class RemoveMethodPatch extends Patch {
	MethodTargetStruct[] targets;
	
	public RemoveMethodPatch(String src, MethodTargetStruct[] targets) {
		super(src);
		this.targets = targets;
	}
	
	@Override
	public int apply(ClassNode node) {
		ArrayList<MethodNode> toRemove = new ArrayList<>();
		
		for (MethodNode method : node.methods)
			for (MethodTargetStruct target : targets)
				if (target.matches(method))
					toRemove.add(method);
		
		for (MethodNode methodNode : toRemove)
			node.methods.remove(methodNode);
		
		return toRemove.size();
	}
	
	@Override
	public int apply(ClassNode classNode, MethodNode method) {
		throw new RuntimeException("no-op");
	}
}
