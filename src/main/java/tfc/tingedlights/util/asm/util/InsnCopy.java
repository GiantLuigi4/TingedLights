package tfc.tingedlights.util.asm.util;

import org.objectweb.asm.tree.*;

public class InsnCopy {
	public static AbstractInsnNode copyOf(AbstractInsnNode src) {
		if (src instanceof MethodInsnNode method) {
			return new MethodInsnNode(
					method.getOpcode(),
					method.owner, method.name,
					method.desc, method.itf
			);
		} else if (src instanceof InsnNode insnNode) {
			return new InsnNode(src.getOpcode());
		} else if (src instanceof VarInsnNode varNode) {
			return new VarInsnNode(varNode.getOpcode(), varNode.var);
		} else if (src instanceof LdcInsnNode ldc) {
			return new LdcInsnNode(ldc.cst);
		} else if (src instanceof LineNumberNode ln) {
			return new LineNumberNode(ln.line, ln.start);
		} else if (src instanceof LabelNode lbl) {
			return new LabelNode(lbl.getLabel());
		} else {
			throw new RuntimeException("NYI: " + src.getClass());
		}
	}
}
