package tfc.tingedlights.util.asm.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MethodCall {
	public final AbstractInsnNode start;
	public final AbstractInsnNode call;
	public final boolean hasThis;
	
	// TODO: figure out where each parameter is, if possible
	
	public MethodCall(boolean findParams, boolean staticMethod, MethodInsnNode insn) {
		this.call = insn;
		this.start = StackTracker.findStart(insn);
		
		boolean hasThis = false;
		if (!staticMethod) {
			if (start instanceof VarInsnNode varInsnNode) {
				// TODO: make this smarter
				hasThis = varInsnNode.var == 0;
			}
		}
		this.hasThis = hasThis;
	}
}
